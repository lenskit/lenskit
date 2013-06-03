/* This file may be freely modified, used, and redistributed without restriction. */
package ${package};

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.io.Serializable;
import java.util.Iterator;

import javax.inject.Inject;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model that maintains the mean offset from the global mean for the ratings
 * for each item.
 * 
 * These offsets can be used for predictions by calling the {@link getGlobalMean} 
 * and {@link getItemOffsets} methods.
 *
 * <p>These computations support mean smoothing (see {@link MeanDamping}).
 * 
 * Users of this model will usually call the Provider's get method to create
 * a suitable model.  The model can be kept around until recomputation is necessary.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@DefaultProvider(ItemMeanModel.Provider.class)
public class ItemMeanModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanModel.class);
    private final double globalMean;
    private final Long2DoubleMap itemOffsets;

    public ItemMeanModel(Long2DoubleMap inItemOffsets, double inGlobalMean) {
        itemOffsets = inItemOffsets;
        globalMean = inGlobalMean;
    }

    /**
     * @return the globalMean
     */
    public double getGlobalMean() {
        return globalMean;
    }

    /**
     * @return the itemMeans
     */
    public Long2DoubleMap getItemOffsets() {
        return itemOffsets;
    }

    public static class Provider implements javax.inject.Provider<ItemMeanModel> {
        private double damping = 0;
        private DataAccessObject dao;

        /**
         * Construct a new provider.
         *
         * @param dao The DAO.
         * @param d   The Bayesian mean damping term for item means. A positive value biases means
         *            towards the global mean.
         */
        @Inject
        public Provider(@Transient DataAccessObject dao, @MeanDamping double d) {
            this.dao = dao;
            damping = d;
        }

        /**
         * Compute item averages from a rating data source. Used in
         * predictors that need this data.  Note that item averages 
         * are actually offsets from the global mean.
         *
         * <p>
         * This method's interface is a little weird, using an output parameter and
         * returning the global mean, so that we can compute the global mean and the
         * item means in a single pass through the data source.
         *
         * @param ratings         The collection of preferences the averages are based on.
         * @param itemOffsetsResult A map in which the means should be stored.
         * @param damping         The damping term.
         * @return The global mean rating. The item means are stored in
         *         {@var itemMeans}.
         */
        public static double computeItemOffsets(Iterator<? extends Rating> ratings, double damping,
                                                 Long2DoubleMap itemOffsetsResult) {
            // We iterate the loop to compute the global and per-item mean
            // ratings.  Subtracting the global mean from each per-item mean
            // is equivalent to averaging the offsets from the global mean, so
            // we can compute the means in parallel and subtract after a single
            // pass through the data.
            double total = 0.0;
            int count = 0;
            itemOffsetsResult.defaultReturnValue(0.0);
            Long2IntMap itemCounts = new Long2IntOpenHashMap();
            itemCounts.defaultReturnValue(0);

            while (ratings.hasNext()) {
                Preference r = ratings.next().getPreference();
                if (r == null) {
                    continue; // skip unrates
                }

                long i = r.getItemId();
                double v = r.getValue();
                total += v;
                count++;
                itemOffsetsResult.put(i, v + itemOffsetsResult.get(i));
                itemCounts.put(i, 1 + itemCounts.get(i));
            }

            final double mean = count > 0 ? total / count : 0;
            logger.debug("Computed global mean {} for {} items",
                         mean, itemOffsetsResult.size());

            logger.debug("Computing item offsets, damping={}", damping);
            LongIterator items = itemCounts.keySet().iterator();
            while (items.hasNext()) {
                long iid = items.nextLong();
                double ct = itemCounts.get(iid) + damping;
                double t = itemOffsetsResult.get(iid) + damping * mean;
                double avg = 0.0;
                if (ct > 0) {
                    avg = t / ct - mean;
                }
                itemOffsetsResult.put(iid, avg);
            }
            return mean;
        }

        @Override
        public ItemMeanModel get() {
            Long2DoubleMap itemOffsetsResult = new Long2DoubleOpenHashMap();
            Cursor<Rating> ratings = dao.getEvents(Rating.class);
            double globalMeanResult = computeItemOffsets(ratings.fast().iterator(), damping, itemOffsetsResult);
            ratings.close();

            return new ItemMeanModel(itemOffsetsResult, globalMeanResult);
        }
    }

}

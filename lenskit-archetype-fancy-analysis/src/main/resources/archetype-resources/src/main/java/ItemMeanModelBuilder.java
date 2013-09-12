package ${package};

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.core.Transient;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.pref.Preference;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
* @author <a href="http://www.grouplens.org">GroupLens Research</a>
*/
public class ItemMeanModelBuilder implements Provider<ItemMeanModel> {
    private static final Logger logger = LoggerFactory.getLogger(ItemMeanModelBuilder.class);
    private double damping = 0;
    private EventDAO dao;

    /**
     * Construct a new provider.
     *
     * @param dao The DAO.  This is {@link Transient}, meaning that it will be used to build the
     *            model but the model, once built, does not depend on it.
     * @param d   The Bayesian mean damping term for item means. A positive value biases means
     *            towards the global mean.
     */
    @Inject
    public ItemMeanModelBuilder(@Transient EventDAO dao, @MeanDamping double d) {
        this.dao = dao;
        damping = d;
    }

    /**
     * Construct the item mean model.
     * @return The item mean model.
     */
    @Override
    public ItemMeanModel get() {
        // We iterate the loop to compute the global and per-item mean
        // ratings.  Subtracting the global mean from each per-item mean
        // is equivalent to averaging the offsets from the global mean, so
        // we can compute the means in parallel and subtract after a single
        // pass through the data.
        double total = 0.0;
        int count = 0;
        // map to sum item ratings
        Long2DoubleMap itemRatingSums = new Long2DoubleOpenHashMap();
        // fastutil lets us specify a defualt value; this makes sums easier to accumulate
        itemRatingSums.defaultReturnValue(0.0);
        // map to count item ratings
        Long2IntMap itemRatingCounts = new Long2IntOpenHashMap();
        itemRatingCounts.defaultReturnValue(0);

        Cursor<Rating> ratings = dao.streamEvents(Rating.class);
        try {
            for (Rating rating: ratings) {
                Preference pref = rating.getPreference();
                if (pref == null) {
                    continue; // skip unrates
                }

                long i = pref.getItemId();
                double v = pref.getValue();
                total += v;
                count++;
                itemRatingSums.put(i, v + itemRatingSums.get(i));
                itemRatingCounts.put(i, 1 + itemRatingCounts.get(i));
            }
        } finally {
            ratings.close();
        }

        final double mean = count > 0 ? total / count : 0;
        logger.debug("Computed global mean {} for {} items",
                     mean, itemRatingSums.size());

        logger.debug("Computing item offsets, damping={}", damping);
        // create a vector to hold item mean offsets
        MutableSparseVector vector = MutableSparseVector.create(itemRatingCounts.keySet());
        // iterate over *all* vector entries, including unset ones
        // we use 'fast iteration' since we won't use entry objects outside the loop
        for (VectorEntry e: vector.fast(VectorEntry.State.EITHER)) {
            final long iid = e.getKey();
            // compute the damped item mean
            final double itemCount = itemRatingCounts.get(iid) + damping;
            final double itemTotal = itemRatingSums.get(iid) + damping * mean;
            if (itemCount > 0) {
                vector.set(e, itemTotal / itemCount - mean);
            }
        }

        return new ItemMeanModel(mean, vector.freeze());
    }
}

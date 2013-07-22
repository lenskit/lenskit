/* This file may be freely modified, used, and redistributed without restriction. */
package ${package};

import org.grouplens.lenskit.baseline.MeanDamping;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.history.RatingVectorUserHistorySummarizer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;

/**
 * Scorer that returns the user's mean offset from item mean rating for all
 * scores.
 *
 * <p>This implements the baseline scorer <i>p<sub>u,i</sub> = mu + b<sub>i</sub> +
 * b<sub>u</sub></i>, where <i>b<sub>i</sub></i> is the item's average rating (less the global
 * mean <i>mu</i>), and <i>b<sub>u</sub></i> is the user's average offset (the average
 * difference between their ratings and the item-mean baseline).
 *
 * <p>It supports mean smoothing (see {@link MeanDamping}).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExtendedItemUserMeanScorer extends AbstractItemScorer {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 22L;
    private static final Logger logger = LoggerFactory.getLogger(ExtendedItemUserMeanScorer.class);

    private final UserEventDAO dao;
    private final double userDamping;  // damping for computing the user averages; more damping biases toward global.
    private final ItemMeanModel model;

    /**
     * Create a new scorer, this assumes ownership of the given map.
     *
     * @param dao The user-event DAO.
     * @param inModel The model.
     * @param inUserDamping The damping term.
     */
    @Inject
    public ExtendedItemUserMeanScorer(UserEventDAO dao, ItemMeanModel inModel,
                                      @MeanDamping double inUserDamping) {
        this.dao = dao;
        model = inModel;
        userDamping = inUserDamping;
    }

    /**
     * Compute the mean offset in user rating from item mean rating.
     *
     * @param ratings the user's rating profile
     * @return the mean offset from item mean rating.
     */
    protected double computeUserOffset(SparseVector ratings) {
        if (ratings.isEmpty()) {
            return 0;
        }

        Collection<Double> values = ratings.values();
        double total = 0;

        for (VectorEntry rating : ratings.fast()) {
            double r = rating.getValue();
            long iid = rating.getKey();
            total += r - getItemMean(iid);
        }
        return total / (values.size() + userDamping);
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        logger.debug("score called to attempt to score %d elements", scores.size());
        UserHistory<Rating> profile = dao.getEventsForUser(user, Rating.class);
        if (profile == null) {
            profile = History.forUser(user, Collections.<Rating>emptyList());
        }
        double meanOffset = computeUserOffset(RatingVectorUserHistorySummarizer.makeRatingVector(profile));
        for (VectorEntry e : scores.fast(VectorEntry.State.EITHER)) {
            scores.set(e, meanOffset + getItemMean(e.getKey()));
        }
    }

    /**
     * Get the mean for a particular item.
     *
     * @param id The item ID.
     * @return The item's mean rating.
     */
    protected double getItemMean(long id) {
        return model.getGlobalMean() + model.getItemOffsets().get(id);
    }
}

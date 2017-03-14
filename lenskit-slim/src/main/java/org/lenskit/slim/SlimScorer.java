package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.results.Results;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.math.Vectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SlimScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(SlimScorer.class);

    protected final SlimModel model;
    private final RatingVectorPDAO rvDAO;


    @Inject
    public SlimScorer(SlimModel m,
                      RatingVectorPDAO dao) {
        model = m;
        rvDAO = dao;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param items The score vector.
     */
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        logger.debug("scoring {} items for user {} with details", items.size(), user);
        Long2DoubleMap ratings = Long2DoubleSortedArrayMap.create(rvDAO.userRatingVector(user));
        List<Result> results = new ArrayList<>();

        for (long item: items) {
            Long2DoubleMap weight = model.getWeights(item);
            double score = Vectors.dotProduct(ratings, weight);
            results.add(Results.create(item, score));
        }
        return Results.newResultMap(results);
    }

}

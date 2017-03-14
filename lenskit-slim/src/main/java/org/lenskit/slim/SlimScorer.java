package org.lenskit.slim;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.*;

import static org.lenskit.slim.LinearRegressionHelper.transposeMap;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SimpleItemItemScorer extends AbstractItemScorer {
    private final SlimBuildContext model;
    private final DataAccessObject dao;
    private final int neighborhoodSize;
    final static Logger logger = LoggerFactory.getLogger(org.lenskit.slim.SimpleItemItemScorer.class);
    //private final SlimUpdateParameters parameters;
    private LinearRegressionAbstract lrModel;

    @Inject
    public SimpleItemItemScorer(SlimBuildContext m, DataAccessObject dao, @LinearRegression LinearRegressionAbstract lrModel) {
        model = m;
        this.dao = dao;
        neighborhoodSize = 20;
        this.lrModel = lrModel;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param items The score vector.  Its key domain is the items to score, and the scores
     *               (rating predictions) should be written back to this vector.
     */
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {

        Long2DoubleMap ratings = getUserRatingVector(user);
        Map<Long,Long2DoubleMap> innerProduct = model.getInnerProducts();

        // TODO Normalize the user's ratings by subtracting the item mean from each one.

        List<Result> results = new ArrayList<>();

        for (long item: items ) {

            //logger.info("current learned weight vector is {}\n and its {}th element is {} ", weights, item, weights.get(item));
            double score = 0.0;
            if (neighborsT.get(user) != null) score = modelSLIM.computePrediction(neighborsT.get(user), weights);
            results.add(Results.create(item, score));
        }
        return Results.newResultMap(results);

    }




}

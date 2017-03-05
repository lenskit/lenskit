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
    private final SimpleItemItemModel model;
    private final DataAccessObject dao;
    private final int neighborhoodSize;
    final static Logger logger = LoggerFactory.getLogger(org.lenskit.slim.SimpleItemItemScorer.class);
    @Inject
    public SimpleItemItemScorer(SimpleItemItemModel m, DataAccessObject dao) {
        model = m;
        this.dao = dao;
        neighborhoodSize = 20;
    }

    /**
     * Score items for a user.
     * @param user The user ID.
     * @param items The score vector.  Its key domain is the items to score, and the scores
     *               (rating predictions) should be written back to this vector.
     */
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap itemMeans = model.getItemMeans();
        Long2DoubleMap ratings = getUserRatingVector(user);
        Map<Long,Long2DoubleMap> innerProduct = model.getInnerproducts();

        // TODO Normalize the user's ratings by subtracting the item mean from each one.

        List<Result> results = new ArrayList<>();

        for (long item: items ) {
            // TODO Compute the user's score for each item, add it to results
            int neighborSize = 100;
            //Map<Long,Long2DoubleMap> neighbors = getItemItemNeighbors(item);
            Map<Long,Long2DoubleMap> neighbors = getItemItemKNN(item, neighborSize);
            Map<Long,Long2DoubleMap> neighborsT = transposeMap(neighbors);
            //NaiveCoordDestLinearRegression modelSLIM = new NaiveCoordDestLinearRegression(3.0, 0.5, false, 10);
            CovarianceUpdateCoordDestLinearRegression modelSLIM = new CovarianceUpdateCoordDestLinearRegression(3.0, 0.5, false, 10);
            Long2DoubleMap labels = getItemRatingVector(item);
            Long2DoubleMap weights = modelSLIM.fit(labels, neighborsT, neighbors, innerProduct, item);
            logger.info("current learned weight vector is {}\n and its {}th element is {} ", weights, item, weights.getOrDefault(item, 0.0));
            double score = 0.0;
            if (neighborsT.get(user) != null) score = modelSLIM.computePrediction(neighborsT.get(user), weights);
            results.add(Results.create(item, score));
        }
        return Results.newResultMap(results);

    }

    /**
     * Get a user's ratings.
     * @param user The user ID.
     * @return The ratings to retrieve.
     */
    private Long2DoubleOpenHashMap getUserRatingVector(long user) {
        List<Rating> history = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get();

        Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap();
        for (Rating r: history) {
            ratings.put(r.getItemId(), r.getValue());
        }

        return ratings;
    }

    /**
     * get a item ratings vector given reference item id
     * @param item item id
     * @return a Long2Double Map whose key is long user id and value is double rating value
     */
    private Long2DoubleMap getItemRatingVector(long item) {
        List<Rating> itemRatings = dao.query(Rating.class)
                                    .withAttribute(CommonAttributes.ITEM_ID, item)
                                    .get();

        Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));
        return LongUtils.frozenMap(ratings);
    }

    /**
     * get K most similar item vectors (computed by cosine similarity) with respect to the given item id
     * @param itemId reference item id
     * @param k number of similar items
     * @return a map comprised of k similar item vectors
     */
    private Map<Long, Long2DoubleMap> getItemItemKNN(long itemId, int k) {
        Long2DoubleMap nrbs = model.getNeighborsInnerProduct(itemId, k);
        Iterator<Long> iter = nrbs.keySet().iterator();
        Map<Long,Long2DoubleMap> itemVectors = Maps.newHashMap();
        while(iter.hasNext()) {
            long item = iter.next();
            List<Rating> itemRatings = dao.query(Rating.class)
                                        .withAttribute(CommonAttributes.ITEM_ID, item)
                                        .get();
            Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));
            itemVectors.put(item, LongUtils.frozenMap(ratings));
        }
        return itemVectors;
    }

    /**
     * get all Item-Item similarities vectors with respect to the given item id
     * @param itemId item id
     * @return a Map which includes all the similarities to the given item
     */
    private Map<Long, Long2DoubleMap> getItemItemNeighbors(long itemId) {
        Long2DoubleMap nrbs = model.getNeighbors(itemId);
        //logger.info("item number is {}",nrbs.keySet().size());
        Iterator<Long> iter = nrbs.keySet().iterator();
        Map<Long,Long2DoubleMap> itemVectors = Maps.newHashMap();
        while(iter.hasNext()) {
            long item = iter.next();
            List<Rating> itemRatings = dao.query(Rating.class)
                    .withAttribute(CommonAttributes.ITEM_ID, item)
                    .get();
            Long2DoubleOpenHashMap ratings = new Long2DoubleOpenHashMap(Ratings.itemRatingVector(itemRatings));
            itemVectors.put(item, LongUtils.frozenMap(ratings));
        }
        return itemVectors;
    }


}

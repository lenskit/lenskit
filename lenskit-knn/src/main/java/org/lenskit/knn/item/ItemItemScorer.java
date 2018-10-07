/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.knn.MinNeighbors;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.results.Results;
import org.lenskit.transform.normalize.UserVectorNormalizer;
import org.lenskit.util.InvertibleFunction;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Score items using an item-item CF model. User ratings are <b>not</b> supplied
 * as default preferences.
 */
public class ItemItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemScorer.class);
    protected final ItemItemModel model;

    private final RatingVectorPDAO rvDAO;
    @Nonnull
    protected final UserVectorNormalizer normalizer;
    @Nonnull
    protected final NeighborhoodScorer scorer;
    private final int neighborhoodSize;
    private final int minNeighbors;

    /**
     * Construct a new item-item scorer.
     *
     * @param dao    The rating vector DAO.
     * @param m      The model
     * @param scorer The neighborhood scorer.
     * @param nnbrs  The number of neighbors.
     * @param min    The minimum number of neighbors.
     */
    @Inject
    public ItemItemScorer(RatingVectorPDAO dao, ItemItemModel m,
                          NeighborhoodScorer scorer,
                          UserVectorNormalizer norm,
                          @NeighborhoodSize int nnbrs,
                          @MinNeighbors int min) {
        rvDAO = dao;
        model = m;
        this.scorer = scorer;
        normalizer = norm;
        neighborhoodSize = nnbrs;
        minNeighbors = min;
        logger.debug("configured item-item scorer with scorer {}", scorer);
    }

    @Nonnull
    public UserVectorNormalizer getNormalizer() {
        return normalizer;
    }

    @Nonnull
    @Override
    public Map<Long, Double> score(long user, @Nonnull Collection<Long> items) {
        logger.debug("scoring {} items for user {}", items.size(), user);
        Long2DoubleMap results = new Long2DoubleOpenHashMap(items.size());
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.basic(results);

        scoreItems(user, items, accum);
        logger.debug("scored {} items for user {}", results.size(), user);

        return results;
    }

    /**
     * Score items by computing predicted ratings.
     */
    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        logger.debug("scoring {} items for user {} with details", items.size(), user);
        List<ItemItemResult> results = new ArrayList<>(items.size());
        ItemItemScoreAccumulator accum = ItemItemScoreAccumulator.detailed(results);

        scoreItems(user, items, accum);
        logger.debug("scored {} items for user {} with details", results.size(), user);

        return Results.newResultMap(results);
    }

    /**
     * Score all items into an accumulator.
     * @param user The user.
     * @param items The items to score.
     * @param accum The accumulator.
     */
    private void scoreItems(long user, @Nonnull Collection<Long> items, ItemItemScoreAccumulator accum) {
        Long2DoubleMap ratings = Long2DoubleSortedArrayMap.create(rvDAO.userRatingVector(user));

        logger.trace("user has {} ratings", ratings.size());
        InvertibleFunction<Long2DoubleMap, Long2DoubleMap> transform = normalizer.makeTransformation(user, ratings);
        Long2DoubleMap itemScores = transform.apply(ratings);

        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            scoreItem(itemScores, item, accum);
        }

        accum.applyReversedTransform(transform);
    }

    protected void scoreItem(Long2DoubleMap userData, long item, ItemItemScoreAccumulator accum) {
        // find the usable neighbors
        Long2DoubleSortedArrayMap allNeighbors = Long2DoubleSortedArrayMap.create(model.getNeighbors(item));
        Long2DoubleMap neighborhood = allNeighbors.subMap(userData.keySet());

        if (neighborhoodSize > 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("truncating {} neighbors to {}", neighborhood.size(), neighborhoodSize);
            }
            Long2DoubleAccumulator acc = new TopNLong2DoubleAccumulator(neighborhoodSize);
            for (Long2DoubleMap.Entry e: neighborhood.long2DoubleEntrySet()) {
                acc.put(e.getLongKey(), e.getDoubleValue());
            }
            neighborhood = acc.finishMap();
        }

        assert neighborhoodSize <= 0 || neighborhood.size() <= neighborhoodSize;
        if (neighborhood.size() < minNeighbors) {
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("scoring item {} with {} of {} neighbors",
                         item, neighborhood.size(), allNeighbors.size());
        }
        scorer.score(item, neighborhood, userData, accum);
    }
}

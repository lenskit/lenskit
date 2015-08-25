/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.grouplens.lenskit.data.dao.UserEventDAO;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.util.UnlimitedScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.knn.MinNeighbors;
import org.lenskit.knn.NeighborhoodSize;
import org.lenskit.knn.item.model.ItemItemModel;
import org.lenskit.results.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Score items using an item-item CF model. User ratings are <b>not</b> supplied
 * as default preferences.
 */
public class ItemItemScorer extends AbstractItemScorer {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemScorer.class);
    public static final Symbol NEIGHBORHOOD_SIZE_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.item.neighborhoodSize");
    protected final ItemItemModel model;

    private final UserEventDAO dao;
    @Nonnull
    protected final UserVectorNormalizer normalizer;
    protected final UserHistorySummarizer summarizer;
    @Nonnull
    protected final NeighborhoodScorer scorer;
    private final int neighborhoodSize;
    private final int minNeighbors;

    /**
     * Construct a new item-item scorer.
     *
     * @param dao    The DAO.
     * @param m      The model
     * @param sum    The history summarizer.
     * @param scorer The neighborhood scorer.
     * @param nnbrs  The number of neighbors.
     * @param min    The minimum number of neighbors.
     */
    @Inject
    public ItemItemScorer(UserEventDAO dao, ItemItemModel m,
                          UserHistorySummarizer sum,
                          NeighborhoodScorer scorer,
                          UserVectorNormalizer norm,
                          @NeighborhoodSize int nnbrs,
                          @MinNeighbors int min) {
        this.dao = dao;
        model = m;
        summarizer = sum;
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

    /**
     * Score items by computing predicted ratings.
     */
    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        UserHistory<? extends Event> history = dao.getEventsForUser(user, summarizer.eventTypeWanted());
        if (history == null) {
            history = History.forUser(user);
        }
        SparseVector summary = summarizer.summarize(history);
        VectorTransformation transform = normalizer.makeTransformation(user, summary);
        MutableSparseVector normed = summary.mutableCopy();
        transform.apply(normed);
        Long2DoubleMap itemScores = normed.asMap();

        List<ItemItemResult> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            ItemItemResult score = scoreItem(itemScores, item);
            if (score != null) {
                results.add(score);
            }
        }

        // de-normalize the results
        MutableSparseVector vec = MutableSparseVector.create(items);
        for (ItemItemResult r: results) {
            vec.set(r.getId(), r.getScore());
        }
        transform.unapply(vec);

        for (int i = results.size() - 1; i >= 0; i--) {
            ItemItemResult r = results.get(i);
            long item = r.getId();
            double score = vec.get(item);
            results.set(i, new ItemItemResult(item, score, r.getNeighborhoodSize()));
        }

        return Results.newResultMap(results);
    }

    protected ItemItemResult scoreItem(Long2DoubleMap userData, long item) {
        SparseVector allNeighbors = model.getNeighbors(item);
        ScoredItemAccumulator acc = null;
        if (neighborhoodSize > 0) {
            // FIXME Abstract accumulator selection logic
            acc = new TopNScoredItemAccumulator(neighborhoodSize);
        } else {
            acc = new UnlimitedScoredItemAccumulator();
        }

        for (VectorEntry e: allNeighbors) {
            if (userData.containsKey(e.getKey())) {
                acc.put(e.getKey(), e.getValue());
            }
        }

        Long2DoubleMap neighborhood = acc.finishMap();
        if (neighborhood.size() < minNeighbors) {
            return null;
        }
        return scorer.score(item, neighborhood, userData);
    }
}

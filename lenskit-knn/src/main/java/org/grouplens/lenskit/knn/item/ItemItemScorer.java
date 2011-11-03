/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;

import java.util.Collection;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.collections.LongSortedArraySet;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.HistorySummarizer;
import org.grouplens.lenskit.data.history.UserVector;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.norm.IdentityVectorNormalizer;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.norm.VectorTransformation;
import org.grouplens.lenskit.params.UserHistorySummary;
import org.grouplens.lenskit.params.UserVectorNormalizer;
import org.grouplens.lenskit.util.ScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Score items using an item-item CF model. User ratings are <b>not</b> supplied
 * as default preferences.
 * 
 * @review Should user ratings be supplied? Optionally?
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see ItemItemRatingPredictor
 */
public class ItemItemScorer extends AbstractItemScorer implements
        ItemItemModelBackedScorer {
    protected final ItemItemModel model;
    protected @Nonnull VectorNormalizer<? super UserVector> normalizer =
        new IdentityVectorNormalizer();
    protected final int neighborhoodSize;
    protected HistorySummarizer summarizer;
    protected @Nonnull NeighborhoodScorer scorer;

    public ItemItemScorer(DataAccessObject dao, ItemItemModel m,
                          @NeighborhoodSize int nnbrs,
                          @UserHistorySummary HistorySummarizer sum,
                          NeighborhoodScorer scorer) {
        super(dao);
        model = m;
        neighborhoodSize = nnbrs;
        summarizer = sum;
        this.scorer = scorer;
    }

    @Override
    public ItemItemModel getModel() {
        return model;
    }

    @Nonnull
    public VectorNormalizer<? super UserVector> getNormalizer() {
        return normalizer;
    }

    /**
     * Set the normalizer to apply to user summaries.
     * 
     * @param norm The normalizer.
     * @see UserVectorNormalizer
     */
    @UserVectorNormalizer
    public void setNormalizer(VectorNormalizer<? super UserVector> norm) {
        normalizer = norm;
    }

    /**
     * Score items by computing predicted ratings.
     * @see #scoreItems(SparseVector, LongSortedSet)
     * @see #makeTransform(UserVector)
     */
    @Override
    public SparseVector score(UserHistory<? extends Event> history,
                              Collection<Long> items) {
        UserVector summary = summarizer.summarize(history);
        VectorTransformation transform = makeTransform(summary);
        MutableSparseVector normed = summary.mutableCopy();
        transform.apply(normed);

        LongSortedSet iset;
        if (items instanceof LongSortedSet) {
            iset = (LongSortedSet) items;
        } else {
            iset = new LongSortedArraySet(items);
        }

        MutableSparseVector preds = scoreItems(normed, iset);

        // untransform the scores
        transform.unapply(preds);
        return preds.freeze();
    }

    /**
     * Compute item scores for a user.
     * 
     * @param userData The user vector for which scores are to be computed.
     * @param items The items to score.
     * @return The scores for the items. The key domain contains all items; only
     *         those items with scores are set.
     */
    protected MutableSparseVector scoreItems(SparseVector userData,
                                             LongSortedSet items) {
        MutableSparseVector scores = new MutableSparseVector(items);
        // We ran reuse accumulators
        ScoredItemAccumulator accum =
            new ScoredItemAccumulator(neighborhoodSize);

        // FIXME Make sure the direction on similarities is right for asym.
        // for each item, compute its prediction
        LongIterator iter = items.iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();

            // find all potential neighbors
            // FIXME: Take advantage of the fact that the neighborhood is sorted
            ScoredLongList neighbors = model.getNeighbors(item);

            if (neighbors == null) {
                /* we cannot predict this item */
                continue;
            }

            // filter and truncate the neighborhood
            ScoredLongListIterator niter = neighbors.iterator();
            while (niter.hasNext()) {
                long oi = niter.nextLong();
                double score = niter.getScore();
                if (userData.containsKey(oi)) {
                    accum.put(oi, score);
                }
            }
            neighbors = accum.finish();

            // compute score & place in vector
            final double score = scorer.score(neighbors, userData);
            if (!Double.isNaN(score)) {
                scores.set(item, score);
            }
        }

        return scores;
    }

    /**
     * Construct a transformation that is used to pre- and post-process
     * summarized user data in {@link #score(UserHistory, Collection)}. The
     * transformation is created from the user summary. It is then applied to
     * the user summary prior to scoring, and unapplied to the scores. Its
     * {@link VectorTransformation#unapply(MutableSparseVector)} method is
     * expected also to populate missing scores as appropriate from the
     * baseline. The vector passed to unapply the transformation will contain
     * all items to be predicted in the key domain, and will have values for all
     * predictable items.
     * 
     * <p>
     * The default implementation delegates to the normalizer
     * ({@link #setNormalizer(VectorNormalizer)}).
     * 
     * @param userData The user summary.
     * @return The transform to pre- and post-process user data.
     */
    protected VectorTransformation makeTransform(final UserVector userData) {
        return normalizer.makeTransformation(userData);
    }

    @Override
    public LongSet getScoreableItems(UserHistory<? extends Event> user) {
        // FIXME This method incorrectly assumes the model is symmetric
        LongSet items = new LongOpenHashSet();
        UserVector summary = summarizer.summarize(user);
        LongIterator iter = summary.keySet().iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            items.addAll(model.getNeighbors(item));
        }
        return items;
    }
}

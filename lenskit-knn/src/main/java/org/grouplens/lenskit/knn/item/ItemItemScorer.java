/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.core.AbstractItemScorer;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.history.UserHistorySummarizer;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.knn.item.model.ItemItemModel;
import org.grouplens.lenskit.transform.normalize.UserVectorNormalizer;
import org.grouplens.lenskit.transform.normalize.VectorTransformation;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.symbols.Symbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * Score items using an item-item CF model. User ratings are <b>not</b> supplied
 * as default preferences.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @see ItemItemRatingPredictor
 */
public class ItemItemScorer extends AbstractItemScorer implements ItemItemModelBackedScorer {
    private static final Logger logger = LoggerFactory.getLogger(ItemItemScorer.class);
    public static final Symbol NEIGHBORHOOD_SIZE_SYMBOL =
            Symbol.of("org.grouplens.lenskit.knn.item.neighborhoodSize");
    protected final ItemItemModel model;

    @Nonnull
    protected UserVectorNormalizer normalizer;
    protected UserHistorySummarizer summarizer;
    @Nonnull
    protected NeighborhoodScorer scorer;
    @Nonnull
    protected ItemScoreAlgorithm algorithm;

    @Inject
    public ItemItemScorer(DataAccessObject dao, ItemItemModel m,
                          UserHistorySummarizer sum,
                          NeighborhoodScorer scorer,
                          ItemScoreAlgorithm algo) {
        super(dao);
        model = m;
        summarizer = sum;
        this.scorer = scorer;
        algorithm = algo;
        logger.info("building item-item scorer with scorer {}", scorer);
    }

    @Override
    public ItemItemModel getModel() {
        return model;
    }

    @Nonnull
    public UserVectorNormalizer getNormalizer() {
        return normalizer;
    }

    /**
     * Set the normalizer to apply to user summaries.
     *
     * @param norm The normalizer.
     * @see UserVectorNormalizer
     */
    @Inject
    public void setNormalizer(UserVectorNormalizer norm) {
        normalizer = norm;
    }

    /**
     * Score items by computing predicted ratings.
     *
     * @see ItemScoreAlgorithm#scoreItems(ItemItemModel, SparseVector, MutableSparseVector, NeighborhoodScorer)
     * @see #makeTransform(long, SparseVector)
     */
    @Override
    public void score(@Nonnull UserHistory<? extends Event> history,
                      @Nonnull MutableSparseVector scores) {
        SparseVector summary = summarizer.summarize(history);
        VectorTransformation transform = makeTransform(history.getUserId(), summary);
        MutableSparseVector normed = summary.mutableCopy();
        transform.apply(normed);

        scores.clear();
        algorithm.scoreItems(model, normed, scores, scorer);

        // untransform the scores
        transform.unapply(scores);
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
     * ({@link #setNormalizer(UserVectorNormalizer)}).
     *
     * @param userData The user summary.
     * @return The transform to pre- and post-process user data.
     */
    protected VectorTransformation makeTransform(long user, SparseVector userData) {
        return normalizer.makeTransformation(user, userData);
    }

    @Override
    public LongSet getScoreableItems(UserHistory<? extends Event> user) {
        // FIXME This method incorrectly assumes the model is symmetric
        LongSet items = new LongOpenHashSet();
        SparseVector summary = summarizer.summarize(user);
        LongIterator iter = summary.keySet().iterator();
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            for (ScoredId id : model.getNeighbors(item)) {
                items.add(id.getId());
            }
        }
        return items;
    }
}

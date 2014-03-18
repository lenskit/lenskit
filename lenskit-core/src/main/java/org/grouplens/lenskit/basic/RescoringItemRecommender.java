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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.ItemRecommender;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.symbols.Symbol;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Item recommender that wraps another item recommender and replaces its scores.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RescoringItemRecommender implements ItemRecommender {
    public static final Symbol ORIGINAL_SCORE_SYMBOL =
            Symbol.of("org.grouplens.lenskit.basic.RescoringItemRecommender.ORIGINAL_SCORE");
    private final ItemRecommender delegate;
    private final ItemScorer scorer;

    /**
     * Create a new rescoring item recommender.
     * @param rec The recommender.
     * @param score The item scorer.
     */
    @Inject
    public RescoringItemRecommender(ItemRecommender rec, ItemScorer score) {
        delegate = rec;
        scorer = score;
    }

    @Override
    public List<ScoredId> recommend(long user) {
        return rescore(user, delegate.recommend(user));
    }

    @Override
    public List<ScoredId> recommend(long user, int n) {
        return rescore(user, delegate.recommend(user, n));
    }

    @Override
    public List<ScoredId> recommend(long user, @Nullable Set<Long> candidates) {
        return rescore(user, delegate.recommend(user, candidates));
    }

    @Override
    public List<ScoredId> recommend(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return rescore(user, delegate.recommend(user, n, candidates, exclude));
    }

    private List<ScoredId> rescore(long user, List<ScoredId> recs) {
        if (recs.isEmpty()) {
            return Collections.emptyList();
        }

        LongList items = new LongArrayList(recs.size());
        for (ScoredId sid: CollectionUtils.fast(recs)) {
            items.add(sid.getId());
        }

        SparseVector scores = scorer.score(user, items);
        ScoredIdListBuilder builder = ScoredIds.newListBuilder(recs.size());
        builder.addChannel(ORIGINAL_SCORE_SYMBOL);
        for (ScoredId sid: CollectionUtils.fast(recs)) {
            // FIXME Make this not allocate so much memory
            builder.add(ScoredIds.copyBuilder(sid)
                                 .setScore(scores.get(sid.getId(), Double.NaN))
                                 .addChannel(ORIGINAL_SCORE_SYMBOL, sid.getScore())
                                 .build());
        }
        return builder.build();
    }
}

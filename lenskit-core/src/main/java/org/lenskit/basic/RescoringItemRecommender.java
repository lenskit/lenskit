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
package org.lenskit.basic;

import com.google.common.collect.ImmutableList;
import org.lenskit.api.*;
import org.lenskit.results.Results;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * Item recommender that wraps a base item recommender and replaces its scores with those produced by another
 * item scorer.  The order from the original recommender is preserved.
 *
 * For performance reasons, the scorer is only invoked by {@link #recommendWithDetails(long, int, Set, Set)}, because
 * scores are not returend in the other recommendation operations.  The results produced by this recommender are of
 * type {@link org.lenskit.results.RescoredResult}; for any recommended item that the scorer cannot score, the result
 * has no score.
 *
 * @see Results#rescore(Result, Result)
 */
public class RescoringItemRecommender implements ItemRecommender {
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
    public List<Long> recommend(long user) {
        return delegate.recommend(user);
    }

    @Override
    public List<Long> recommend(long user, int n) {
        return delegate.recommend(user);
    }

    @Override
    public List<Long> recommend(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        return delegate.recommend(user, n, candidates, exclude);
    }

    @Override
    public ResultList recommendWithDetails(long user, int n, @Nullable Set<Long> candidates, @Nullable Set<Long> exclude) {
        ResultList raw = delegate.recommendWithDetails(user, n, candidates, exclude);
        ResultMap newScores = scorer.scoreWithDetails(user, raw.idList());
        ImmutableList.Builder<Result> rescored = ImmutableList.builder();
        for (Result r: raw) {
            Result s = newScores.get(r.getId());
            rescored.add(Results.rescore(r, s));
        }
        return Results.newResultList(rescored.build());
    }
}

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

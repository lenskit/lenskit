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
package org.lenskit.rerank;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.api.ItemRecommender;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.basic.AbstractItemRecommender;
import org.lenskit.results.Results;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A hybrid item recommender that uses a greedy re-ranking strategy to allow re-ranking of items iteratively. This
 * general algorithm is commonly employed to efficiently optimize for set properties of a recommendation list such as
 * inter-item diveristy.
 *
 * This algorithm takes a baseline ranking algorithm, gets the top-n recommendations and re-ranks them iteratively.
 * To select each recommended item, first a scoring algorithm is ran based on the currently selected recommendations
 * and each candidate item. The item with the highest score is then added to the recommended list. This process repeates
 * until enough items are recommended.
 *
 * @author Daniel Kluver
 */
public class GreedyRerankingItemRecommender extends AbstractItemRecommender {
    private static final Logger logger = LoggerFactory.getLogger(GreedyRerankingItemRecommender.class);
    private final ItemRecommender baseRecommender;
    private final GreedyRerankStrategy strategy;

    @Inject
    public GreedyRerankingItemRecommender(ItemRecommender baseRecommender, GreedyRerankStrategy strategy) {
        this.baseRecommender = baseRecommender;
        this.strategy = strategy;
    }


    @Override
    protected ResultList recommendWithDetails(long user, int n, @Nullable LongSet candidateItems, @Nullable LongSet exclude) {
        List<Result> candidates = baseRecommender.recommendWithDetails(user, -1, candidateItems, exclude);
        //modifiable copy
        candidates = new ArrayList<>(candidates);
        if (n<0) {
            n = candidates.size();
        }

        List<Result> results = new ArrayList<>(n);
        for (int i = 0; i<n; i++) {
            final Result nextItem = strategy.nextItem(user, n, results, candidates);
            if (nextItem == null) {
                break;
            } else {
                Iterables.removeIf(candidates, new Predicate<Result>() {
                    @Override
                    public boolean apply(@Nullable Result input) {
                        return input!= null && input.getId() == nextItem.getId();
                    }
                });
                results.add(nextItem);
            }
        }
        return Results.newResultList(results);
    }
}

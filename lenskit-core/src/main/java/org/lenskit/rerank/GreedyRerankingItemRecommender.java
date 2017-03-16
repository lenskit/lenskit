/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

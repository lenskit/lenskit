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

import org.lenskit.api.Result;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Interface for classes that select the next item to recommend from a list of candidate items. This class
 * is used in {@link GreedyRerankingItemRecommender} to define the greedy strategy for selecting the next item.
 * Often this will be a search for the best item that satisfies some constraint or the best item by some metric that
 * takes in account what items have already been recommended.
 *
 * @author Daniel Kluver
 */


public interface GreedyRerankStrategy {
    /**
     * A method to select the next item to be recommended. This method will be called many times in the process of
     * generating recommendations. Therefore this method should consider optimzation options such as only searching a
     * constant number of the candidates list before picking an item.
     *
     * @param userId the id of the user receiving these recommendation.
     * @param n the total number of items that will be recommended.
     * @param items The list of items already chosen to be recommended in recommendation order
     * @param candidates A ranked list of all items in the system that can still be recommended in this context. Given in
     *                   ranking order (which is not strictly guaranteed to be in score order, but often will be).
     * @return A {@link Result} object noting which candidate item should be added to the recommendation list.
     * The object returned will be directly added to the result list, therefore implementations should use a custom
     * Result subclass if there is any interesting information about the recommendation process to be returned.
     * Alternatively, this method can return a null to indicate that there is no item that can be added to the list without
     * violating a constraint and that the recommendation list should be prematurely terminated.
     */
    @Nullable Result nextItem(long userId, int n, List<? extends Result> items, List<? extends Result> candidates);
}

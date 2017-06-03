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

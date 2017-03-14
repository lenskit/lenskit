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
import org.lenskit.api.ResultList;

import java.util.List;

/**
 * Interface for classes that produce scores for the value of adding an item to a list of recommended items. This class
 * is used in {@link GreedyRerankingItemRecommender} to define the metric to optimize when recommending items.
 *
 * While this interface does not have support for hard filtering, a filter behavior can be approximated by returning
 * {@link Double#MIN_VALUE}. Filtered items may still be returned, but only after all non-filtered items are eliminated.
 *
 * @author Daniel Kluver
 */
public interface Rescorer {

    /**
     * Compute a recommendation score for a given item in the context of a list of already chosen items.
     * @param items - The already chosen items and their <i>baseline scores</i> (not the scores returned by this object).
     *              It is likely that the baseline scores will be predictions.
     * @param nextItem - The posisble item to be added (and its baseline score).
     * @return - a score representing how good of an item this is to recommend. Higher scores are more likely to be
     * selected for recommendation.
     */
    double score(List<? extends Result> items, Result nextItem);
}

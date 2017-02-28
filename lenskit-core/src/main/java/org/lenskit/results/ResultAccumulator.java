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
package org.lenskit.results;

import org.lenskit.api.Result;
import org.lenskit.api.ResultList;
import org.lenskit.api.ResultMap;
import org.lenskit.util.collections.SortedListAccumulator;

import javax.annotation.Nonnull;

/**
 * Accumulator for sorted lists of results.  This class will return result lists, with the highest-scored result first.
 *
 * Create one with {@link #create(int)}.
 */
public class ResultAccumulator {
    private final SortedListAccumulator<Result> accum;

    /**
     * Create a new result accumulator.
     * @param n The number of results desired; negative for unlimited.
     * @return A result accumulator.
     */
    public static ResultAccumulator create(int n) {
        return new ResultAccumulator(SortedListAccumulator.decreasing(n, Results.scoreOrder()));
    }

    private ResultAccumulator(SortedListAccumulator<Result> acc) {
        accum = acc;
    }

    /**
     * Add a result to the accumulator.
     * @param r The result to add.
     */
    public void add(@Nonnull Result r) {
        accum.add(r);
    }

    /**
     * Add a basic result to the accumulator.
     * @param item The item ID to add.
     * @param score The score to add.
     */
    public void add(long item, double score) {
        add(Results.create(item, score));
    }

    /**
     * Finish accumulating and return the accumulated results.
     *
     * When this method is called, the accumulator is reset and can be used to accumulate a fresh set of results.
     *
     * @return The accumulated results, in nonincreasing order of score.
     */
    public ResultList finish() {
        return Results.newResultList(accum.finish());
    }

    public ResultMap finishMap() {
        return Results.newResultMap(accum.finish());
    }
}

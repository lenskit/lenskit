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

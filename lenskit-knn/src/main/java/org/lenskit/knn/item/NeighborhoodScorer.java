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
package org.lenskit.knn.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Compute scores from neighborhoods and score vectors.
 *
 * <p>
 * This interface encapsulates aggregating user scores and item similarities into a final
 * score. The neighborhood is pre-filtered to only contain items for which
 * scores are available, and truncated to the neighborhood size, so all functions
 * implementing this interface need to do is accumulate scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(WeightedAverageNeighborhoodScorer.class)
public interface NeighborhoodScorer {
    /**
     * Compute a score based on similar neighbors and their corresponding
     * scores.
     *
     * @param item      The item ID to score.
     * @param neighbors A vector of neighbors with similarity measures.
     * @param scores    A vector of item scores. It should contain a score for
     *                  every item in <var>neighbors</var>.
     * @param accum     An accumulator to receive the score computed by this method.
     */
    void score(long item, Long2DoubleMap neighbors, Long2DoubleMap scores, ItemItemScoreAccumulator accum);
}

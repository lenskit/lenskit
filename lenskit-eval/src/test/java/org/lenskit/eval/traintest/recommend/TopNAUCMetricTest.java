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
package org.lenskit.eval.traintest.recommend;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Test;

import java.util.BitSet;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;

public class TopNAUCMetricTest {
    @Test
    public void testAUCOfAllGoodIsOne() {
        assertThat(TopNAUCMetric.computeAUC(new int[] {0,1,2,3,4}, 5, 5),
                   closeTo(1.0, 1.0e-6));
    }

    @Test
    public void testAUCOfAllBadIsZero() {
        assertThat(TopNAUCMetric.computeAUC(new int[] {}, 5, 5),
                   closeTo(0.0, 1.0e-6));
    }

    @Test
    public void testAUCOfAltIsAboutHalf() {
        // a little under
        assertThat(TopNAUCMetric.computeAUC(new int[] {1, 3, 5, 7, 9}, 10, 5),
                   closeTo(0.4, 1.0e-3));
        // a little over
        assertThat(TopNAUCMetric.computeAUC(new int[] {0, 2, 4, 6, 8}, 10, 5),
                   closeTo(0.6, 1.0e-3));
    }
}
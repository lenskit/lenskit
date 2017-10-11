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
package org.lenskit.util.math;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.math.MutualInformationAccumulator;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MutualInformationAccumulatorTest {
    MutualInformationAccumulator accum;

    private Matcher<Double> closeTo(double v) {
        return Matchers.closeTo(v, 1.0e-5);
    }

    @Before
    public void newAccumulator() {
        accum = new MutualInformationAccumulator(4);
    }

    @Test
    public void testNoData() {
        // if there is no data, entropy is undefined
        assertThat(accum.getMutualInformation(),
                   closeTo(0));
        assertThat(accum.getV1Entropy(),
                   closeTo(0));
        assertThat(accum.getV2Entropy(),
                   closeTo(0));
    }

    /**
     * Several data points, uniform distribution, matched.
     */
    @Test
    public void testUniformFullMI() {
        for (int i = 0; i < 24; i++) {
            accum.count(i % 4, i % 4);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(2));
    }

    /**
     * Several data points, uniform distribution, consistent but not same.
     */
    @Test
    public void testUniformFullMI2() {
        for (int i = 0; i < 24; i++) {
            accum.count(i % 4, 3 - (i + 2) % 4);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(2));
    }

    /**
     * Completely independent variables.
     */
    @Test
    public void testIndependent() {
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                accum.count(i % 4, j % 4);
            }
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(0));
    }

    /**
     * Half-explanatory variable. For each i, there are two different j values.
     */
    @Test
    public void testHalfExplain() {
        for (int i = 0; i < 32; i++) {
            int x = i % 4;
            int y = (x + (i / 4) % 2) % 4;
            accum.count(x, y);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(1));
    }
}

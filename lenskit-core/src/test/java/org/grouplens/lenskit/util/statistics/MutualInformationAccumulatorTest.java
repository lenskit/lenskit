/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.statistics;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
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

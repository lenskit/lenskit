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
package org.lenskit.util.math;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RollingWindowMeanAccumulatorTest {

    private static final double PRECISION = 0.0001;

    @Test
    public void testInitialize() {
        RollingWindowMeanAccumulator avg = new RollingWindowMeanAccumulator();
        Assert.assertEquals(0, avg.getMean(), PRECISION);
        Assert.assertEquals(0, avg.getSize());
    }

    @Test
    public void testOtherInitialize() {
        RollingWindowMeanAccumulator avg = new RollingWindowMeanAccumulator(10, 1);
        Assert.assertEquals(1, avg.getMean(), PRECISION);
        Assert.assertEquals(10, avg.getSize());

        for (int i = 1; i<11; i++) {
            avg.add(2);
            Assert.assertEquals(1+i/10.0, avg.getMean(), PRECISION);
            Assert.assertEquals(10, avg.getSize());
        }
    }

    @Test
    public void testAdd() {
        RollingWindowMeanAccumulator avg = new RollingWindowMeanAccumulator(10);
        Assert.assertEquals(0.0, avg.getMean(), PRECISION);
        Assert.assertEquals(0, avg.getSize());

        // averages running up to size
        for(int i = 1; i<11; i++) {
            avg.add(i);
            Assert.assertEquals(i*(i+1)/2.0/i, avg.getMean(), PRECISION);
            Assert.assertEquals(i, avg.getSize());
        }

        // average doesn't change with same data added
        for(int i = 1; i<11; i++) {
            avg.add(i);
            Assert.assertEquals(5.5, avg.getMean(), PRECISION);
            Assert.assertEquals(10, avg.getSize());

        }

        // average changes if you fill up with new data
        for(int i = 1; i<11; i++) {
            avg.add(7);
        }
        Assert.assertEquals(7, avg.getMean(), PRECISION);
    }

}

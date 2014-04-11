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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.11
 */
public class MeanAccumulatorTest {

    private static final double PRECISION = 0.0001;

    @Test
    public void testInitialize() {
        MeanAccumulator avg = new MeanAccumulator();
        Assert.assertEquals(0, avg.getMean(), PRECISION);
        Assert.assertEquals(0, avg.getCount());
    }

    @Test
    public void testInitializeAdvanced() {
        MeanAccumulator avg = new MeanAccumulator(4, 2);
        Assert.assertEquals(2, avg.getMean(), PRECISION);
        Assert.assertEquals(2, avg.getCount());

        avg.add(5);
        Assert.assertEquals(3, avg.getMean(), PRECISION);
        Assert.assertEquals(3, avg.getCount());
    }

    @Test
    public void testAdd() {
        MeanAccumulator avg = new MeanAccumulator();
        Assert.assertEquals(0.0, avg.getMean(), PRECISION);
        Assert.assertEquals(0, avg.getCount());

        avg.add(10);
        Assert.assertEquals(10, avg.getMean(), PRECISION);
        Assert.assertEquals(1, avg.getCount());

        avg.add(3);
        Assert.assertEquals(6.5, avg.getMean(), PRECISION);
        Assert.assertEquals(2, avg.getCount());

        avg.add(7);
        Assert.assertEquals(6.666666, avg.getMean(), PRECISION);
        Assert.assertEquals(3, avg.getCount());

        avg.add(13);
        Assert.assertEquals(8.25, avg.getMean(), PRECISION);
        Assert.assertEquals(4, avg.getCount());

        avg.add(21);
        Assert.assertEquals(10.8, avg.getMean(), PRECISION);
        Assert.assertEquals(5, avg.getCount());
    }

    @Test
    public void testGetAverage() {
        MeanAccumulator avg = new MeanAccumulator();
        Assert.assertEquals(0.0, avg.getMean(), PRECISION);

        avg.add(5);
        Assert.assertEquals(5.0, avg.getMean(), PRECISION);

        avg.add(13);
        Assert.assertEquals(9, avg.getMean(), PRECISION);
    }

    @Test
    public void testGetCount() {
        MeanAccumulator avg = new MeanAccumulator();
        Assert.assertEquals(0, avg.getCount());

        avg.add(5);
        Assert.assertEquals(1, avg.getCount());

        avg.add(13);
        Assert.assertEquals(2, avg.getCount());
    }
}

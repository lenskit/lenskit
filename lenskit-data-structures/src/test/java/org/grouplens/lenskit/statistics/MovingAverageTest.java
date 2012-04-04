/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.statistics;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthias.Balke <matthias.balke@tu-dortmund.de>
 * @since 0.10
 * 
 */
public class MovingAverageTest {

    private static final double PRECISION = 0.0001;

    /**
     * Test method for {@link org.grouplens.lenskit.statistics.MovingAverage} .
     */
    @Test
    public void testInitialize() {
        MovingAverage avg = new MovingAverage();
        Assert.assertEquals(0, avg.getAverage(), PRECISION);
        Assert.assertEquals(0, avg.getCount());
    }

    /**
     * Test method for {@link org.grouplens.lenskit.statistics.MovingAverage} .
     */
    @Test
    public void testInitializeAdvanced() {
        MovingAverage avg = new MovingAverage(2, 2);
        Assert.assertEquals(2, avg.getAverage(), PRECISION);
        Assert.assertEquals(2, avg.getCount());

        avg.add(5);
        Assert.assertEquals(3, avg.getAverage(), PRECISION);
        Assert.assertEquals(3, avg.getCount());
    }

    /**
     * Test method for
     * {@link org.grouplens.lenskit.statistics.MovingAverage#add(double)} .
     */
    @Test
    public void testAdd() {
        MovingAverage avg = new MovingAverage();
        Assert.assertEquals(0.0, avg.getAverage(), PRECISION);
        Assert.assertEquals(0, avg.getCount());

        avg.add(10);
        Assert.assertEquals(10, avg.getAverage(), PRECISION);
        Assert.assertEquals(1, avg.getCount());

        avg.add(3);
        Assert.assertEquals(6.5, avg.getAverage(), PRECISION);
        Assert.assertEquals(2, avg.getCount());

        avg.add(7);
        Assert.assertEquals(6.666666, avg.getAverage(), PRECISION);
        Assert.assertEquals(3, avg.getCount());

        avg.add(13);
        Assert.assertEquals(8.25, avg.getAverage(), PRECISION);
        Assert.assertEquals(4, avg.getCount());

        avg.add(21);
        Assert.assertEquals(10.8, avg.getAverage(), PRECISION);
        Assert.assertEquals(5, avg.getCount());
    }

    /**
     * Test method for
     * {@link org.grouplens.lenskit.statistics.MovingAverage#getAverage()} .
     */
    @Test
    public void testGetAverage() {
        MovingAverage avg = new MovingAverage();
        Assert.assertEquals(0.0, avg.getAverage(), PRECISION);

        avg.add(5);
        Assert.assertEquals(5.0, avg.getAverage(), PRECISION);

        avg.add(13);
        Assert.assertEquals(9, avg.getAverage(), PRECISION);
    }

    /**
     * Test method for
     * {@link org.grouplens.lenskit.statistics.MovingAverage#getCount()} .
     */
    @Test
    public void testGetCount() {
        MovingAverage avg = new MovingAverage();
        Assert.assertEquals(0, avg.getCount());

        avg.add(5);
        Assert.assertEquals(1, avg.getCount());

        avg.add(13);
        Assert.assertEquals(2, avg.getCount());
    }
}

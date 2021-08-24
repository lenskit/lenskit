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

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
package org.lenskit.mf.svd;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;
import org.lenskit.data.ratings.PreferenceDomain;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DomainClampingKernelTest {
    BiasedMFKernel kernel = new DomainClampingKernel(PreferenceDomain.fromString("[1,5]"));

    @Test
    public void testEmptyVectors() throws Exception {
        assertThat(kernel.apply(Math.PI, MatrixUtils.createRealVector(new double [0]), MatrixUtils.createRealVector(new double [0])),
                   equalTo(Math.PI));
    }

    @Test
    public void testBasicVectors() throws Exception {
        RealVector uv = MatrixUtils.createRealVector(new double[]{0.1, 0.2});
        RealVector iv = MatrixUtils.createRealVector(new double[]{0.1, -0.5});
        assertThat(kernel.apply(Math.PI, uv, iv),
                   closeTo(Math.PI + 0.01 - 0.1, 1.0e-5));
    }

    @Test
    public void testClamping() throws Exception {
        RealVector uv = MatrixUtils.createRealVector(new double[]{2, 0.2});
        RealVector iv = MatrixUtils.createRealVector(new double[]{2, -0.5});
        assertThat(kernel.apply(3, uv, iv),
                   closeTo(4.9, 1.0e-5));
    }
}

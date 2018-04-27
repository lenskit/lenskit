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
package org.lenskit.transform.quantize;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.data.ratings.PreferenceDomain;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainQuantizerTest {
    PreferenceDomain domain;

    @Before
    public void setUp() {
        domain = new PreferenceDomain(0.5, 5.0, 0.5);
    }

    @Test
    public void testMakeValues() {
        RealVector vals = PreferenceDomainQuantizer.makeValues(domain);
        RealVector evals = new ArrayRealVector(new double[]{0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0});
        assertThat(vals.getDimension(), equalTo(evals.getDimension()));
        for (int i = 0; i < vals.getDimension(); i++) {
            assertThat("element " + i,
                       vals.getEntry(i),
                       closeTo(evals.getEntry(i), 1.0e-6));
        }
    }

    @Test
    public void testHalfStars() {
        Quantizer q = new PreferenceDomainQuantizer(domain);
        assertThat(q.getCount(), equalTo(10));
        assertThat(q.getIndexValue(q.index(4.9)), closeTo(5.0, 1.0e-6));
        assertThat(q.getIndexValue(q.index(4.7)), closeTo(4.5, 1.0e-6));
        assertThat(q.getIndexValue(q.index(3.42)), closeTo(3.5, 1.0e-6));
        assertThat(q.quantize(4.9), closeTo(5.0, 1.0e-6));
        assertThat(q.quantize(4.7), closeTo(4.5, 1.0e-6));
        assertThat(q.quantize(3.42), closeTo(3.5, 1.0e-6));
    }
}

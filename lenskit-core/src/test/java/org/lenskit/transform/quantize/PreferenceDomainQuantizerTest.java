/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.transform.quantize;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
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

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
package org.grouplens.lenskit.util;

import net.java.quickcheck.collection.Pair;
import org.junit.Test;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.somePairs;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MathUtilsTest {
    @Test
    public void testZeroIsZero() {
        assertThat(MathUtils.isZero(0), equalTo(true));
    }

    @Test
    public void testZeroIsZeroWithEpsilon() {
        assertThat(MathUtils.isZero(0, 1.0e-6), equalTo(true));
    }

    @Test
    public void testOneIsNotZero() {
        assertThat(MathUtils.isZero(1), equalTo(false));
    }

    @Test
    public void testOneIsNotZeroWithEpsilon() {
        assertThat(MathUtils.isZero(1, 1.0e-6), equalTo(false));
    }

    @Test
    public void testSmallIsZero() {
        assertThat(MathUtils.isZero(1.0e-6, 1.0e-5),
                   equalTo(true));
    }

    @Test
    public void testManyNumbersMightBeZero() {
        for (Pair<Double,Double> pair: somePairs(doubles(-10, 10), doubles(0, 2))) {
            assertThat(MathUtils.isZero(pair.getFirst(), pair.getSecond()),
                       equalTo(Math.abs(pair.getFirst()) < pair.getSecond()));
        }
    }
}

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

import net.java.quickcheck.collection.Pair;
import org.junit.Test;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.somePairs;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ScalarsTest {
    @Test
    public void testZeroIsZero() {
        assertThat(Scalars.isZero(0), equalTo(true));
    }

    @Test
    public void testZeroIsZeroWithEpsilon() {
        assertThat(Scalars.isZero(0, 1.0e-6), equalTo(true));
    }

    @Test
    public void testOneIsNotZero() {
        assertThat(Scalars.isZero(1), equalTo(false));
    }

    @Test
    public void testOneIsNotZeroWithEpsilon() {
        assertThat(Scalars.isZero(1, 1.0e-6), equalTo(false));
    }

    @Test
    public void testSmallIsZero() {
        assertThat(Scalars.isZero(1.0e-6, 1.0e-5),
                   equalTo(true));
    }

    @Test
    public void testManyNumbersMightBeZero() {
        for (Pair<Double,Double> pair: somePairs(doubles(-10, 10), doubles(0, 2))) {
            assertThat(Scalars.isZero(pair.getFirst(), pair.getSecond()),
                       equalTo(Math.abs(pair.getFirst()) < pair.getSecond()));
        }
    }
}

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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.junit.Test;

import java.util.Map;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someMaps;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.longs;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PreferenceDomainTest {
    @Test
    public void testParseContinuous() {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,3.0]");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(3.0, 1.0e-6));
        assertFalse(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(0.0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseInverted() {
        PreferenceDomain.fromString("[2.5, -1]");
    }

    @Test
    public void testParseDiscrete() {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,5.0]/0.5");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(5.0, 1.0e-6));
        assertTrue(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(0.5));
    }

    @Test
    public void testParseInt() {
        PreferenceDomain d = PreferenceDomain.fromString("[ 1 , 5 ] / 1");
        assertThat(d.getMinimum(), closeTo(1.0, 1.0e-6));
        assertThat(d.getMaximum(), closeTo(5.0, 1.0e-6));
        assertTrue(d.hasPrecision());
        assertThat(d.getPrecision(), equalTo(1.0));
    }

    @Test
    public void testClampValue()  {
        PreferenceDomain d = PreferenceDomain.fromString("[1.0,5.0]");
        for (Map<Long,Double> vec: someMaps(longs(), doubles(0.0, 8.0))) {
            Long2DoubleMap clamped = d.clampVector(vec);
            assertThat(clamped.keySet(), equalTo(vec.keySet()));

            for (Long k: vec.keySet()) {
                double v = vec.get(k);
                if (v < 1.0) {
                    assertThat(clamped, hasEntry(k, 1.0));
                } else if (v > 5.0) {
                    assertThat(clamped, hasEntry(k, 5.0));
                } else {
                    assertThat(clamped, hasEntry(k, v));
                }
            }
        }
    }
}

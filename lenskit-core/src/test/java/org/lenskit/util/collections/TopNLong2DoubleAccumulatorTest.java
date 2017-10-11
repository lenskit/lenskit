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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongList;
import org.junit.Before;
import org.junit.Test;
import org.lenskit.util.collections.Long2DoubleAccumulator;
import org.lenskit.util.collections.TopNLong2DoubleAccumulator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TopNLong2DoubleAccumulatorTest {
    Long2DoubleAccumulator accum;

    @Before
    public void createAccumulator() {
        accum = new TopNLong2DoubleAccumulator(3);
    }

    @Test
    public void testEmpty() {
        LongList out = accum.finishList();
        assertTrue(out.isEmpty());
    }

    @Test
    public void testAccumMap() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        Long2DoubleMap out = accum.finishMap();
        assertThat(out.size(), equalTo(3));
        assertThat(out, hasEntry(2L, 9.8));
        assertThat(out, hasEntry(5L, 4.2));
        assertThat(out, hasEntry(3L, 2.9));
    }

    @Test
    public void testAccumMapLimit() {
        accum.put(7, 1.0);
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        accum.put(8, 2.1);
        Long2DoubleMap out = accum.finishMap();
        assertThat(out.size(), equalTo(3));
        assertThat(out, hasEntry(2L, 9.8));
        assertThat(out, hasEntry(5L, 4.2));
        assertThat(out, hasEntry(3L, 2.9));
    }

    @Test
    public void testAccumList() {
        accum.put(5, 4.2);
        accum.put(3, 2.9);
        accum.put(2, 9.8);
        LongList out = accum.finishList();
        assertThat(out, contains(2L, 5L, 3L));
    }
}

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
package org.lenskit.data.store;

import org.junit.Test;
import org.lenskit.util.math.Scalars;

import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static net.java.quickcheck.generator.PrimitiveGenerators.integers;
import static net.java.quickcheck.generator.iterable.Iterables.toIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class DoubleShardTest {
    @Test
    public void testInitialState() {
        DoubleShard shard = DoubleShard.create();
        assertThat(shard.size(), equalTo(0));
    }

    @Test
    public void testAddObject() {
        DoubleShard shard = DoubleShard.create();
        shard.put(0, 3.5);
        assertThat(shard.size(), equalTo(1));
        assertThat(shard.get(0), equalTo(3.5));
        assertThat(shard.isNull(0), equalTo(false));
    }

    @Test
    public void testAddObjectLater() {
        DoubleShard shard = DoubleShard.create();
        shard.put(5, 3.5);
        assertThat(shard.size(), equalTo(6));
        assertThat(shard.get(5), equalTo(3.5));
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.isNull(4), equalTo(true));
        assertThat(shard.isNull(5), equalTo(false));
    }

    @Test
    public void testClearObject() {
        DoubleShard shard = DoubleShard.create();
        shard.put(0, 3.5);
        shard.put(1, 7.0);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(7.0));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testPutClearAdapt() {
        DoubleShard shard = DoubleShard.create();
        shard.put(0, 3.5);
        shard = shard.adapt(7.8);
        shard.put(1, 7.8);
        shard.put(0, null);
        assertThat(shard.size(), equalTo(2));
        assertThat(shard.get(0), nullValue());
        assertThat(shard.isNull(0), equalTo(true));
        assertThat(shard.get(1), equalTo(7.8));
        assertThat(shard.isNull(1), equalTo(false));
    }

    @Test
    public void testStorableInts() {
        for (int n: toIterable(integers(-512, 512), 10000)) {
            double d = n;
            assertThat(String.format("value %f should be %s", d, Math.abs(n) < 64 ? "storable" : "unstorable"),
                       DoubleShard.Compact.isStorable(d),
                       equalTo(n > -64 && n < 64));
        }
    }

    @Test
    public void testStorableHalfPrecision() {
        for (int n: toIterable(integers(-127, 127), 10000)) {
            double d = n / 2.0;
            assertThat(String.format("value %f should be storable", d),
                       DoubleShard.Compact.isStorable(d),
                       equalTo(true));
        }
    }

    @Test
    public void testStorableQuarterPrecision() {
        for (int n: toIterable(integers(-255, 255), 10000)) {
            double d = n / 4.0;
            assertThat(String.format("value %f should be %s", d, n % 2 == 0 ? "storable" : "unstorable"),
                       DoubleShard.Compact.isStorable(d),
                       equalTo(n % 2 == 0));
        }
    }

    @Test
    public void testStorableDoubles() {
        for (double d: toIterable(doubles(), 10000)) {
            assertThat(DoubleShard.Compact.isStorable(d),
                       equalTo(d > -64 && d < 64 && Scalars.isZero(Math.IEEEremainder(d, 0.5))));
        }
    }
}
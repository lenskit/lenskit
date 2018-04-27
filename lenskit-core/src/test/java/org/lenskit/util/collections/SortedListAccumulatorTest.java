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

import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class SortedListAccumulatorTest {
    @Test
    public void testEmptyLimitedAccum() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(5);
        assertThat(acc.finish(), hasSize(0));
    }

    @Test
    public void testLimitedAddOne() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(5);
        acc.add("foo");
        List<String> res = acc.finish();
        assertThat(res, hasSize(1));
        assertThat(res, contains("foo"));
    }

    @Test
    public void testLimitedAddThree() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(5);
        acc.add("foo");
        acc.add("bar");
        acc.add("zed");
        List<String> res = acc.finish();
        assertThat(res, hasSize(3));
        assertThat(res, contains("zed", "foo", "bar"));
    }

    @Test
    public void testLimitedAddThreeLimit2() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(2);
        acc.add("foo");
        acc.add("bar");
        acc.add("zed");
        List<String> res = acc.finish();
        assertThat(res, hasSize(2));
        assertThat(res, contains("zed", "foo"));
    }

    @Test
    public void testEmptyUnlimitedAccum() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(-1);
        assertThat(acc.finish(), hasSize(0));
    }

    @Test
    public void testUnlimitedAddOne() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(-1);
        acc.add("foo");
        List<String> res = acc.finish();
        assertThat(res, hasSize(1));
        assertThat(res, contains("foo"));
    }

    @Test
    public void testUnlimitedAddThree() {
        SortedListAccumulator<String> acc = SortedListAccumulator.decreasing(-1);
        acc.add("foo");
        acc.add("bar");
        acc.add("zed");
        List<String> res = acc.finish();
        assertThat(res, hasSize(3));
        assertThat(res, contains("zed", "foo", "bar"));
    }
}

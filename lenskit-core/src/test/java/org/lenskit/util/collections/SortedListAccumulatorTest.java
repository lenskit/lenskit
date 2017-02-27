/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

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
package org.grouplens.lenskit.cursors;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.sameInstance;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class AbstractPollingCursorTest {
    private static class EmptyPC extends AbstractPollingCursor<String> {
        @Override
        protected String poll() {
            return null;
        }
    }

    private static class ListPC extends AbstractPollingCursor<String> {
        private final List<String> strings;
        int n = 0;

        public static ListPC make(String... args) {
            return new ListPC(Arrays.asList(args));
        }

        public ListPC(List<String> strs) {
            super(strs.size());
            strings = strs;
        }
        @Override
        protected String poll() {
            if (n < strings.size()) {
                n++;
                return strings.get(n-1);
            } else {
                return null;
            }
        }
    }

    private static class NumPC extends AbstractPollingCursor<Number> {
        private AtomicInteger number = new AtomicInteger();
        private final int max;
        public NumPC(int m) {
            max = m;
        }

        @Override
        public Number poll() {
            if (number.getAndIncrement() < max) {
                return number;
            } else {
                return null;
            }
        }

        @Override
        public Number copy(Number n) {
            return n.intValue();
        }
    }

    @Test
    public void testEmptyPollingCursor() {
        Cursor<String> cur = new EmptyPC();
        try {
            assertThat(cur.hasNext(), equalTo(false));
            try {
                cur.next();
                fail("next should fail");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        } finally {
            cur.close();
        }
    }

    @Test
    public void testEmptyNextFirst() {
        Cursor<String> cur = new EmptyPC();
        try {
            try {
                cur.next();
                fail("next should fail");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        } finally {
            cur.close();
        }
    }

    @Test
    public void testSingletonCursor() {
        Cursor<String> cur = ListPC.make("foo");
        try {
            assertThat(cur.hasNext(), equalTo(true));
            assertThat(Cursors.makeList(cur),
                       contains("foo"));
        } finally {
            cur.close();
        }
    }

    @Test
    public void testMultipleCursor() {
        Cursor<String> cur = ListPC.make("foo", "bar", "blatz");
        assertThat(cur.getRowCount(), equalTo(3));
        List<String> strs = Cursors.makeList(cur);
        assertThat(strs, hasSize(3));
        assertThat(strs, contains("foo", "bar", "blatz"));
    }

    @Test
    public void testMultipleCursorWithMore() {
        // the poll will (bogusly) return more items after returning null, these should be ignored
        Cursor<String> cur = ListPC.make("foo", "bar", "blatz", null, "wombat");
        List<String> strs = Cursors.makeList(cur);
        assertThat(strs, hasSize(3));
        assertThat(strs, contains("foo", "bar", "blatz"));
    }

    @Test
    public void testCopyingCursorFast() {
        Cursor<Number> nums = new NumPC(3);
        try {
            assertThat(nums.hasNext(), equalTo(true));
            Number first = nums.fastNext();
            assertThat(first.intValue(), equalTo(1));
            Number n2 = nums.fastNext();
            assertThat(n2.intValue(), equalTo(2));
            assertThat(n2, sameInstance(first));
            n2 = nums.fastNext();
            assertThat(n2.intValue(), equalTo(3));
            assertThat(n2, sameInstance(first));
            try {
                nums.fastNext();
                fail("firstNext should fail after checking");
            } catch (NoSuchElementException e) {
                /* expected */
            }
            assertThat(nums.hasNext(), equalTo(false));
        } finally {
            nums.close();
        }
    }

    @Test
    public void testCopyingCursor() {
        Cursor<Number> nums = new NumPC(3);
        try {
            List<Number> list = Cursors.makeList(nums);
            assertThat(list, hasSize(3));
            assertThat(list, contains((Number) 1, 2, 3));
        } finally {
            nums.close();
        }
    }
}

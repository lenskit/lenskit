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

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Tests for the cursor utility methods.  This has the side effect of also testing some of the
 * general cursor support code.
 */
public class CursorsTest {
    //region The empty cursor
    @Test
    public void testEmptyCursor() {
        Cursor<String> cur = Cursors.empty();
        try {
            assertThat(cur.hasNext(), equalTo(false));
            assertThat(cur.getRowCount(), equalTo(0));
            try {
                cur.next();
                fail("next() on empty cursor should fail");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        } finally {
            cur.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testEmptyCursorFastNext() {
        Cursor<String> cur = Cursors.empty();
        try {
            assertThat(cur.hasNext(), equalTo(false));
            try {
                cur.fastNext();
                fail("next() on empty cursor should fail");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        } finally {
            cur.close();
        }
    }

    @Test
    public void testEmptyCursorIterable() {
        Cursor<String> cur = Cursors.empty();
        try {
            assertThat(Iterables.isEmpty(cur),
                       equalTo(true));
        } finally {
            cur.close();
        }
    }
    //endregion

    //region Collection and Iterator Wrapping
    @Test
    public void testWrapEmptyCollection() {
        Cursor<?> cursor = Cursors.wrap(Collections.emptyList());
        try {
            assertThat(cursor.getRowCount(),
                       equalTo(0));
            assertThat(cursor.hasNext(),
                       equalTo(false));
            try {
                cursor.next();
                fail("next should fail on empty cursor");
            } catch (NoSuchElementException e) {
            /* expected */
            }
        } finally {
            cursor.close();
        }
    }

    @Test
    public void testWrapCollection() {
        Cursor<String> cursor = Cursors.wrap(Lists.newArrayList("foo", "bar"));
        try {
            assertThat(cursor.getRowCount(),
                       equalTo(2));
            assertThat(cursor.hasNext(),
                       equalTo(true));
            assertThat(cursor.next(),
                       equalTo("foo"));
            assertThat(cursor.next(),
                       equalTo("bar"));
            assertThat(cursor.hasNext(),
                       equalTo(false));
            try {
                cursor.next();
                fail("next should fail on empty cursor");
            } catch (NoSuchElementException e) {
            /* expected */
            }
        } finally {
            cursor.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testWrapCollectionFastNext() {
        Cursor<String> cursor = Cursors.wrap(Lists.newArrayList("foo", "bar"));
        try {
            assertThat(cursor.getRowCount(),
                       equalTo(2));
            assertThat(cursor.hasNext(),
                       equalTo(true));
            assertThat(cursor.fastNext(),
                       equalTo("foo"));
            assertThat(cursor.fastNext(),
                       equalTo("bar"));
            assertThat(cursor.hasNext(),
                       equalTo(false));
            try {
                cursor.fastNext();
                fail("fastNext should fail on empty cursor");
            } catch (NoSuchElementException e) {
            /* expected */
            }
        } finally {
            cursor.close();
        }
    }

    @Test
    public void testWrapCollectionIterator() {
        Cursor<String> cursor = Cursors.wrap(Lists.newArrayList("foo", "bar"));
        try {
            List<String> strs = Lists.newArrayList(cursor.iterator());
            assertThat(strs, hasSize(2));
            assertThat(strs, contains("foo", "bar"));
        } finally {
            cursor.close();
        }
    }

    @Test
    public void testWrapIterator() {
        Cursor<String> cursor = Cursors.wrap(Lists.newArrayList("foo", "bar").iterator());
        try {
            assertThat(cursor.getRowCount(), lessThan(0));
            // since collection wrapping tested general capabilities, these tests will be terser
            assertThat(cursor,
                       contains("foo", "bar"));
        } finally {
            cursor.close();
        }
    }
    //endregion

    //region Making Collections
    @Test
    public void testMakeListEmpty() {
        List<?> lst = Cursors.makeList(Cursors.empty());
        assertThat(lst, hasSize(0));
    }

    @Test
    public void testMakeListOfIterator() {
        String[] strings = { "READ ME", "ZELGO NER", "HACKEM MUCHE" };
        // go through an iteator so the cursor doesn't know its length
        List<String> lst = Cursors.makeList(Cursors.wrap(Iterators.forArray(strings)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(strings));
    }

    @Test
    public void testMakeListOfList() {
        // same test as previous, but with a cursor with a known length
        String[] strings = { "READ ME", "ZELGO NER", "HACKEM MUCHE" };
        List<String> lst = Cursors.makeList(Cursors.wrap(Arrays.asList(strings)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(strings));
    }
    //endregion

    //region Concat
    @Test
    public void testEmptyConcat() {
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = Cursors.concat();
        assertThat(cursor.hasNext(), equalTo(false));
        try {
            cursor.next();
            fail("next on empty cursor should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testConcatEmpty() {
        Cursor<String> cursor = Cursors.concat(Cursors.<String>empty());
        assertThat(cursor.hasNext(), equalTo(false));
        try {
            cursor.next();
            fail("next on empty cursor should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testConcatOne() {
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = Cursors.concat(Cursors.of("foo"));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo("foo"));
        assertThat(cursor.hasNext(), equalTo(false));
        try {
            cursor.next();
            fail("next on consumed cursor should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testConcatTwo() {
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = Cursors.concat(Cursors.of("foo", "bar"));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo("foo"));
        assertThat(cursor.next(), equalTo("bar"));
        assertThat(cursor.hasNext(), equalTo(false));
        try {
            cursor.next();
            fail("next on consumed cursor should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testConcatTwoCursors() {
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = Cursors.concat(Cursors.of("foo"), Cursors.of("bar"));
        assertThat(cursor.hasNext(), equalTo(true));
        assertThat(cursor.next(), equalTo("foo"));
        assertThat(cursor.next(), equalTo("bar"));
        assertThat(cursor.hasNext(), equalTo(false));
        try {
            cursor.next();
            fail("next on consumed cursor should fail");
        } catch (NoSuchElementException e) {
            /* expected */
        }
    }

    @Test
    public void testConcatWithEmpty() {
        @SuppressWarnings("unchecked")
        Cursor<String> cursor = Cursors.concat(Cursors.of("foo"),
                                               Cursors.<String>empty(),
                                               Cursors.of("bar"));
        assertThat(cursor, contains("foo", "bar"));
        assertThat(cursor.hasNext(), equalTo(false));
    }
    //endregion

    @Test
    public void testConsumeNone() {
        Cursor<String> cur = Cursors.of("foo", "bar");
        cur = Cursors.consume(0, cur);
        assertThat(cur, contains("foo", "bar"));
    }

    @Test
    public void testConsumeOne() {
        Cursor<String> cur = Cursors.of("foo", "bar");
        cur = Cursors.consume(1, cur);
        assertThat(cur, contains("bar"));
    }

    @Test
    public void testConsumeTooMany() {
        Cursor<String> cur = Cursors.of("foo", "bar");
        cur = Cursors.consume(3, cur);
        assertThat(cur, contains());
    }
}

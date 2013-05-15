package org.grouplens.lenskit.cursors;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.*;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for the cursor utility methods.  This has the side effect of also testing some of the
 * general cursor support code.
 */
public class TestCursors {
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

    @Test
    public void testMakeListOfLongIterator() {
        long[] nums = { 42, 39, 67 };
        // go through an iteator so the cursor doesn't know its length
        LongList lst = Cursors.makeList(Cursors.wrap(LongIterators.wrap(nums)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(42L, 39L, 67L));
    }

    @Test
    public void testMakeListOfLongList() {
        // same test as previous, but with a cursor with a known length
        long[] nums = { 42, 39, 67 };
        LongList lst = Cursors.makeList(Cursors.wrap(LongArrayList.wrap(nums)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(42L, 39L, 67L));
    }

    @Test
    public void testMakeSetOfLongs() {
        long[] nums = { 42, 39, 67, 39 };
        LongSet lst = Cursors.makeSet(Cursors.wrap(LongArrayList.wrap(nums)));
        assertThat(lst, hasSize(3));
        assertThat(lst, containsInAnyOrder(42L, 39L, 67L));
    }
    //endregion
}

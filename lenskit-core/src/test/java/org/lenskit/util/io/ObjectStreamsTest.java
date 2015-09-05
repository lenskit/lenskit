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
package org.lenskit.util.io;

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
 * Tests for the stream utility methods.  This has the side effect of also testing some of the
 * general stream support code.
 */
public class ObjectStreamsTest {
    //region The empty stream
    @Test
    public void testEmptyStream() {
        ObjectStream<String> cur = ObjectStreams.empty();
        try {
            assertThat(cur.readObject(), nullValue());
            try {
                cur.iterator().next();
                fail("next() on empty stream should fail");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        } finally {
            cur.close();
        }
    }

    @Test
    public void testEmptyStreamIterable() {
        ObjectStream<String> cur = ObjectStreams.empty();
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
        ObjectStream<?> objectStream = ObjectStreams.wrap(Collections.emptyList());
        try {
            assertThat(objectStream.readObject(), nullValue());
            try {
                objectStream.iterator().next();
                fail("next should fail on empty stream");
            } catch (NoSuchElementException e) {
            /* expected */
            }
        } finally {
            objectStream.close();
        }
    }

    @Test
    public void testWrapCollection() {
        ObjectStream<String> objectStream = ObjectStreams.wrap(Lists.newArrayList("foo", "bar"));
        try {
            assertThat(objectStream.readObject(),
                       equalTo("foo"));
            assertThat(objectStream.readObject(),
                       equalTo("bar"));
            assertThat(objectStream.readObject(),
                       nullValue());
        } finally {
            objectStream.close();
        }
    }

    @Test
    public void testWrapCollectionIterator() {
        ObjectStream<String> objectStream = ObjectStreams.wrap(Lists.newArrayList("foo", "bar"));
        try {
            List<String> strs = Lists.newArrayList(objectStream.iterator());
            assertThat(strs, hasSize(2));
            assertThat(strs, contains("foo", "bar"));
        } finally {
            objectStream.close();
        }
    }

    @Test
    public void testWrapIterator() {
        ObjectStream<String> stream = ObjectStreams.wrap(Lists.newArrayList("foo", "bar").iterator());
        try {
            // since collection wrapping tested general capabilities, these tests will be terser
            assertThat(stream, contains("foo", "bar"));
        } finally {
            stream.close();
        }
    }
    //endregion

    //region Making Collections
    @Test
    public void testMakeListEmpty() {
        List<?> lst = ObjectStreams.makeList(ObjectStreams.empty());
        assertThat(lst, hasSize(0));
    }

    @Test
    public void testMakeListOfIterator() {
        String[] strings = { "READ ME", "ZELGO NER", "HACKEM MUCHE" };
        // go through an iteator so the stream doesn't know its length
        List<String> lst = ObjectStreams.makeList(ObjectStreams.wrap(Iterators.forArray(strings)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(strings));
    }

    @Test
    public void testMakeListOfList() {
        // same test as previous, but with a stream with a known length
        String[] strings = { "READ ME", "ZELGO NER", "HACKEM MUCHE" };
        List<String> lst = ObjectStreams.makeList(ObjectStreams.wrap(Arrays.asList(strings)));
        assertThat(lst, hasSize(3));
        assertThat(lst, contains(strings));
    }
    //endregion

    //region Concat
    @Test
    public void testEmptyConcat() {
        @SuppressWarnings("unchecked")
        ObjectStream<String> objectStream = ObjectStreams.concat();
        assertThat(objectStream.readObject(), nullValue());
    }
    @SuppressWarnings("unchecked")
    @Test
    public void testConcatEmpty() {
        ObjectStream<String> objectStream = ObjectStreams.concat(ObjectStreams.<String>empty());
        assertThat(objectStream.readObject(), nullValue());
    }

    @Test
    public void testConcatOne() {
        @SuppressWarnings("unchecked")
        ObjectStream<String> objectStream = ObjectStreams.concat(ObjectStreams.of("foo"));
        assertThat(objectStream, contains("foo"));
    }

    @Test
    public void testConcatTwo() {
        @SuppressWarnings("unchecked")
        ObjectStream<String> objectStream = ObjectStreams.concat(ObjectStreams.of("foo", "bar"));
        assertThat(objectStream, contains("foo", "bar"));
    }

    @Test
    public void testConcatTwoStreams() {
        @SuppressWarnings("unchecked")
        ObjectStream<String> objectStream = ObjectStreams.concat(ObjectStreams.of("foo"), ObjectStreams.of("bar"));
        assertThat(objectStream, contains("foo", "bar"));
    }

    @Test
    public void testConcatWithEmpty() {
        @SuppressWarnings("unchecked")
        ObjectStream<String> objectStream = ObjectStreams.concat(ObjectStreams.of("foo"),
                                                                 ObjectStreams.<String>empty(),
                                                                 ObjectStreams.of("bar"));
        assertThat(objectStream, contains("foo", "bar"));
        assertThat(objectStream.readObject(), nullValue());
    }
    //endregion

    @Test
    public void testConsumeNone() {
        ObjectStream<String> cur = ObjectStreams.of("foo", "bar");
        cur = ObjectStreams.consume(0, cur);
        assertThat(cur, contains("foo", "bar"));
    }

    @Test
    public void testConsumeOne() {
        ObjectStream<String> cur = ObjectStreams.of("foo", "bar");
        cur = ObjectStreams.consume(1, cur);
        assertThat(cur, contains("bar"));
    }

    @Test
    public void testConsumeTooMany() {
        ObjectStream<String> cur = ObjectStreams.of("foo", "bar");
        cur = ObjectStreams.consume(3, cur);
        assertThat(cur.readObject(), nullValue());
    }
}

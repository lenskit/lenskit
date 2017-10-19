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
package org.lenskit.util.io;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.java.quickcheck.Generator;
import org.junit.Test;
import org.lenskit.data.ratings.Rating;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.java.quickcheck.generator.PrimitiveGenerators.integers;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.lenskit.util.test.LenskitGenerators.ratings;

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

    /* The following tests are for the stream implementation methods. Boilerplate, but we want to
     * make sure they route correctly. */
    //region Stream method tests
    private List<Rating> makeRatings() {
        Generator<Rating> rgen = ratings();
        int n = integers(10, 1000).next();

        return Stream.generate(rgen::next)
                     .limit(n)
                     .collect(Collectors.toList());
    }

    @Test
    public void testFilter() {
        List<Rating> ratings = makeRatings();
        List<Rating> correct = ratings.stream()
                                      .filter(r -> r.getUserId() % 2 == 1)
                                      .collect(Collectors.toList());
        List<Rating> result = ObjectStreams.wrap(ratings)
                                           .filter(r -> r.getUserId() % 2 == 1)
                                           .collect(Collectors.toList());
        assertThat(result, equalTo(correct));
    }

    @Test
    public void testMapToInt() {
        List<Rating> ratings = makeRatings();
        int correct = ratings.stream()
                             .mapToInt(r -> (int) r.getUserId())
                             .sum();
        int result = ObjectStreams.wrap(ratings)
                                  .mapToInt(r -> (int) r.getUserId())
                                  .sum();
        assertThat(result, equalTo(correct));
    }

    @Test
    public void testMapToLong() {
        List<Rating> ratings = makeRatings();
        long correct = ratings.stream()
                             .mapToLong(Rating::getUserId)
                             .sum();
        long result = ObjectStreams.wrap(ratings)
                                  .mapToLong(Rating::getUserId)
                                  .sum();
        assertThat(result, equalTo(correct));
    }

    @Test
    public void testMapToDouble() {
        List<Rating> ratings = makeRatings();
        double correct = ratings.stream()
                                .mapToDouble(Rating::getValue)
                                .sum();
        double result = ObjectStreams.wrap(ratings)
                                     .mapToDouble(Rating::getValue)
                                     .sum();
        assertThat(result, equalTo(correct));
    }

    //endregion
}

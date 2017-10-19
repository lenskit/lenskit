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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import java.io.Closeable;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Utility methods for streams.
 *
 * @compat Public
 */
public final class ObjectStreams {
    private ObjectStreams() {
    }

    /**
     * Wrap an iterator in an object stream.
     *
     * The iterator may not contain `null`. This property is checked lazily; the object stream will not fail
     * until the `null` would be returned.
     *
     * @param <T>      The type of data to return.
     * @param iterator An iterator to wrap
     * @return An object stream returning the elements of the iterator.
     */
    public static <T> ObjectStream<T> wrap(Iterator<? extends T> iterator) {
        return new IteratorObjectStream<>(iterator);
    }

    /**
     * Wrap a Java stream in an object stream.
     *
     * The stream may not contain `null`. This property is checked lazily; the object stream will not fail
     * until the `null` would be returned.
     *
     * @param <T>      The type of data to return.
     * @param stream   A stream to wrap
     * @return An object stream returning the elements of the stream.
     */
    public static <T> ObjectStream<T> wrap(Stream<? extends T> stream) {
        return new IteratorObjectStream<>(stream.iterator());
    }

    /**
     * Wrap a Java stream in an object stream.
     *
     * The stream may not contain `null`. This property is checked lazily; the object stream will not fail
     * until the `null` would be returned.
     *
     * @param <T>      The type of data to return.
     * @param stream   A stream to wrap
     * @param root     The 'root stream', which will be closed when the resulting stream is closed.
     * @return An object stream returning the elements of the stream.
     */
    public static <T> ObjectStream<T> wrap(Stream<? extends T> stream,
                                           @WillCloseWhenClosed Closeable root) {
        return new IteratorObjectStream<>(stream.iterator(), root);
    }

    /**
     * Wrap a collection in an object stream.
     *
     * The iterator may not contain `null`. This property is checked lazily; the object stream will not fail
     * until the `null` would be returned.
     *
     * @param <T>        The type of data to return.
     * @param collection A collection to wrap
     * @return An object stream returning the elements of the collection.
     */
    public static <T> ObjectStream<T> wrap(Collection<? extends T> collection) {
        return new IteratorObjectStream<>(collection);
    }

    /**
     * Filter an object stream.
     *
     * @param <T>       The type of object stream rows.
     * @param stream    The source stream.
     * @param predicate A predicate indicating which rows to return.
     * @return An object stream returning all rows for which <var>predicate</var> returns
     *         {@code true}.
     */
    public static <T> ObjectStream<T> filter(@WillCloseWhenClosed ObjectStream<T> stream, Predicate<? super T> predicate) {
        return new FilteredObjectStream<>(stream, predicate);
    }

    /**
     * Filter an object stream to only contain elements of type <var>type</var>. Unlike
     * {@link #filter(ObjectStream, Predicate)} with a predicate from
     * {@link Predicates#instanceOf(Class)}, this method also transforms the
     * stream to be of the target type.
     *
     * @param <T>    The type of value in the stream.
     * @param source The source stream.
     * @param type   The type to filter.
     * @return An object stream returning all elements in <var>stream</var> which are
     *         instances of type <var>type</var>.
     */
    public static <T> ObjectStream<T> filter(@WillCloseWhenClosed final ObjectStream<?> source, final Class<T> type) {
        return new AbstractObjectStream<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T readObject() {
                Object obj = source.readObject();
                while (obj != null && !type.isInstance(obj)) {
                    obj = source.readObject();
                }
                return type.cast(obj);
            }

            @Override
            public void close() {
                source.close();
            }
        };
    }

    /**
     * Consume and discard the first {@code n} elements from an object stream.
     * @param n The number of elements to drop.
     * @param stream The stream.
     * @param <T> The stream's element type.
     * @return The passed-in stream, for convenience and functional-looking code.  This method immediately consumes the
     * elements, however, so {@code stream} is modified.
     */
    public static <T> ObjectStream<T> consume(int n, ObjectStream<T> stream) {
        Preconditions.checkArgument(n >= 0, "number to skip must be non-negative");
        boolean wasNull = false;
        for (int i = 0; i < n && !wasNull; i++) {
            T obj = stream.readObject();
            wasNull = obj == null;
        }
        return stream;
    }

    /**
     * Transform an object stream's values.
     *
     * @param <S>      The type of source stream rows
     * @param <T>      The type of output stream rows
     * @param objectStream   The source stream
     * @param function A function to apply to each row in the stream.  It will be applied to each value at most once.
     * @return A new stream iterating the results of <var>function</var>.
     * @deprecated Use {@link ObjectStream#map(java.util.function.Function)}, possibly with {@link #wrap(Stream, Closeable)}.
     */
    @Deprecated
    public static <S, T> ObjectStream<T> transform(@WillCloseWhenClosed ObjectStream<S> objectStream, Function<? super S, ? extends T> function) {
        return new TransformedObjectStream<>(objectStream, function);
    }

    /**
     * Create an empty object stream.
     *
     * @param <T> The type of value in the object stream.
     * @return An empty object stream.
     */
    public static <T> ObjectStream<T> empty() {
        return wrap(Collections.<T>emptyList());
    }

    /**
     * Read an object stream into a list, closing when it is finished.
     *
     * @param <T>    The type of item in the object stream.
     * @param objectStream The object stream.
     * @return A new list containing the elements of the object stream.
     * @deprecated Use {@link ObjectStream#collect(Collector)}.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    @Deprecated
    public static <T> List<T> makeList(@WillClose ObjectStream<? extends T> objectStream) {
        List<T> result = null;
        try {
            if (objectStream instanceof IteratorObjectStream) {
                result = ((IteratorObjectStream) objectStream).getList();
            }
            if (result == null) {
                ImmutableList.Builder<T> builder = ImmutableList.builder();
                builder.addAll(objectStream);
                result = builder.build();
            }
        } finally {
            objectStream.close();
        }

        return result;
    }

    /**
     * Count the items in a stream.
     *
     * @param objectStream The object stream.
     * @return The number of items in the stream.
     * @deprecated Use {@link ObjectStream#collect(Collector)}.
     */
    @SuppressWarnings("PMD.LooseCoupling")
    @Deprecated
    public static int count(@WillClose ObjectStream<?> objectStream) {
        try {
            if (objectStream instanceof IteratorObjectStream) {
                List<?> list  = ((IteratorObjectStream) objectStream).getList();
                if (list != null) {
                    return list.size();
                }
            }

            int n = 0;
            Object obj = objectStream.readObject();
            while (obj != null) {
                n++;
                obj = objectStream.readObject();
            }
            return n;
        } finally {
            objectStream.close();
        }
    }

    /**
     * Sort an object stream.  This reads the original object stream into a list, sorts it, and
     * returns a new object stream backed by the list (after closing the original object stream).
     *
     * @param <T>    The type of value in the object stream.
     * @param objectStream The object stream to sort.
     * @param comp   The comparator to use to sort the object stream.
     * @return An object stream iterating over the sorted results.
     * @deprecated Use {@link ObjectStream#sorted(Comparator)}, possibly with {@link #wrap(Stream, Closeable)}.
     */
    @Deprecated
    public static <T> ObjectStream<T> sort(@WillClose ObjectStream<T> objectStream,
                                           Comparator<? super T> comp) {
        ArrayList<T> list;
        try {
            list = Lists.newArrayList(objectStream);
        } finally {
            objectStream.close();
        }
        Collections.sort(list, comp);
        return wrap(list);
    }

    /**
     * Create an object stream over a fixed set of elements. This is mostly useful for testing.
     * @param contents The contents.
     * @param <T> The data type.
     * @return The object stream.
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // method is safe MDE 2015-05-08
    public static <T> ObjectStream<T> of(T... contents) {
        return wrap(Arrays.asList(contents));
    }

    /**
     * Concatenate object streams.  Each object stream is closed as closed as it is consumed.
     * @param streams The object streams to concatenate.
     * @param <T> The type of data.
     * @return The concatenated object stream.
     */
    public static <T> ObjectStream<T> concat(Iterable<? extends ObjectStream<? extends T>> streams) {
        return new SequencedObjectStream<>(streams);
    }

    /**
     * Concatenate object streams.
     * @see #concat(Iterable)
     */
    @SafeVarargs
    @SuppressWarnings("varargs") // method is safe MDE 2015-05-08
    public static <T> ObjectStream<T> concat(ObjectStream<? extends T>... objectStreams) {
        return concat(Arrays.asList(objectStreams));
    }
}

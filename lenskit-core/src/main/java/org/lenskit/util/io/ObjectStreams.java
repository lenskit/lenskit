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

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.WillClose;
import javax.annotation.WillCloseWhenClosed;
import java.util.*;

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
        return new IteratorObjectStream<>(collection.iterator());
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
     * @param stream The source stream.
     * @param type   The type to filter.
     * @return An object stream returning all elements in <var>stream</var> which are
     *         instances of type <var>type</var>.
     */
    public static <T> ObjectStream<T> filter(@WillCloseWhenClosed final ObjectStream<?> stream, final Class<T> type) {
        return new AbstractObjectStream<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T readObject() {
                Object obj = stream.readObject();
                while (obj != null && !type.isInstance(obj)) {
                    obj = stream.readObject();
                }
                return type.cast(obj);
            }

            @Override
            public void close() {
                stream.close();
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
     * @param function A function to apply to each row in the stream.
     * @return A new stream iterating the results of <var>function</var>.
     */
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
     */
    @SuppressWarnings("PMD.LooseCoupling")
    public static <T> ArrayList<T> makeList(@WillClose ObjectStream<? extends T> objectStream) {
        ArrayList<T> list;
        try {
            list = new ArrayList<>();
            for (T item : objectStream) {
                list.add(item);
            }
        } finally {
            objectStream.close();
        }

        return list;
    }

    /**
     * Sort an object stream.  This reads the original object stream into a list, sorts it, and
     * returns a new object stream backed by the list (after closing the original object stream).
     *
     * @param <T>    The type of value in the object stream.
     * @param objectStream The object stream to sort.
     * @param comp   The comparator to use to sort the object stream.
     * @return An object stream iterating over the sorted results.
     */
    public static <T> ObjectStream<T> sort(@WillClose ObjectStream<T> objectStream,
                                     Comparator<? super T> comp) {
        final ArrayList<T> list = makeList(objectStream);
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

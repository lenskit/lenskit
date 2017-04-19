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
package org.lenskit.util.io;

import com.google.common.collect.*;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Base class to make {@link ObjectStream}s easier to implement.
 *
 * @param <T> The type of value returned by this stream.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public abstract class AbstractObjectStream<T> implements ObjectStream<T> {
    protected Stream<T> stream;

    /**
     * No-op implementation of the {@link ObjectStream#close()} method.
     */
    @Override
    public void close() {
        // no-op
    }

    /**
     * Get the iterator.  This method just returns {@code this}, so for-each
     * loops can be used over streams.
     *
     * @return The stream as an iterator.
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return new ObjectStreamIterator<>(this);
    }

    private Stream<T> stream() {
        if (stream == null) {
            stream = Streams.stream(iterator());
        }
        return stream;
    }

    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return stream().filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return stream().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return stream().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream().flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return stream().distinct();
    }

    @Override
    public Stream<T> sorted() {
        return stream().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return stream().sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return stream().peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return stream().limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return stream().skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        stream().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        stream().forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return stream().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream().max(comparator);
    }

    @Override
    public long count() {
        return stream().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return stream().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return stream().findAny();
    }

    @Override
    public Spliterator<T> spliterator() {
        return stream().spliterator();
    }

    @Override
    public boolean isParallel() {
        return stream().isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return stream().sequential();
    }

    @Override
    public Stream<T> parallel() {
        return stream().parallel();
    }

    @Override
    public Stream<T> unordered() {
        return stream().unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return stream().onClose(closeHandler);
    }
}

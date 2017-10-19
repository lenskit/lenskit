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
package org.lenskit.results;

import it.unimi.dsi.fastutil.BidirectionalIterator;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import org.lenskit.api.Result;
import org.lenskit.util.keys.KeyedObjectMap;

import net.jcip.annotations.Immutable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Standard result map implementation.
 */
@Immutable
public class BasicResultMap extends AbstractLong2ObjectMap<Result> implements LenskitResultMap {
    private static final long serialVersionUID = 1L;

    private final KeyedObjectMap<Result> delegate;

    /**
     * Create a new result map from a collection of results.
     * @param objs The results.
     */
    public BasicResultMap(Iterable<? extends Result> objs) {
        this(new KeyedObjectMap<>(objs, Results.keyExtractor()));
    }

    BasicResultMap(KeyedObjectMap<Result> dlg) {
        delegate = dlg;
    }

    @Override
    public Long2DoubleSortedMap scoreMap() {
        return new ScoreMapImpl();
    }

    @Override
    public Iterator<Result> iterator() {
        return delegate.values().iterator();
    }

    @Override
    public ObjectSet<Entry<Result>> long2ObjectEntrySet() {
        return delegate.long2ObjectEntrySet();
    }

    @Override
    public Result get(long l) {
        return delegate.get(l);
    }

    @Override
    public boolean containsKey(long l) {
        return delegate.containsKey(l);
    }

    @Override
    public ObjectCollection<Result> values() {
        return delegate.values();
    }

    @Override
    public ObjectSet<Map.Entry<Long, Result>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public LongSet keySet() {
        return delegate.keySet();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public double getScore(long id) {
        Result r = delegate.get(id);
        if (r == null) {
            return Double.NaN;
        } else {
            return r.getScore();
        }
    }

    private class ScoreMapImpl extends AbstractLong2DoubleSortedMap {
        private static final long serialVersionUID = 1L;

        @Override
        public ObjectSortedSet<Entry> long2DoubleEntrySet() {
            return new ScoreMapEntrySet();
        }

        @Override
        public double get(long l) {
            return getScore(l);
        }

        @Override
        public boolean containsKey(long l) {
            return delegate.containsKey(l);
        }

        @Override
        public LongSortedSet keySet() {
            return delegate.keySet();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public LongComparator comparator() {
            // TODO Implement this method
            return null;
        }

        @Override
        public Long2DoubleSortedMap subMap(long l, long l1) {
            return new BasicResultMap(delegate.subMap(l, l1))
                    .scoreMap();
        }

        @Override
        public Long2DoubleSortedMap headMap(long l) {
            return new BasicResultMap(delegate.headMap(l))
                    .scoreMap();
        }

        @Override
        public Long2DoubleSortedMap tailMap(long l) {
            return new BasicResultMap(delegate.tailMap(l))
                    .scoreMap();
        }

        @Override
        public long firstLongKey() {
            return delegate.firstLongKey();
        }

        @Override
        public long lastLongKey() {
            return delegate.lastLongKey();
        }
    }

    private class ScoreMapEntrySet extends AbstractObjectSortedSet<Long2DoubleMap.Entry> implements AbstractLong2DoubleMap.FastEntrySet {
        @Override
        public ObjectBidirectionalIterator<Long2DoubleMap.Entry> iterator() {
            return new ScoreMapEntryIter();
        }

        @Override
        public ObjectIterator<Long2DoubleMap.Entry> fastIterator() {
            return new ScoreMapEntryFastIter();
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public ObjectBidirectionalIterator<Long2DoubleMap.Entry> iterator(Long2DoubleMap.Entry entry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObjectSortedSet<Long2DoubleMap.Entry> subSet(Long2DoubleMap.Entry entry, Long2DoubleMap.Entry k1) {
            return new BasicResultMap(delegate.subMap(entry.getLongKey(), k1.getLongKey()))
                    .scoreMap()
                    .long2DoubleEntrySet();
        }

        @Override
        public ObjectSortedSet<Long2DoubleMap.Entry> headSet(Long2DoubleMap.Entry entry) {
            return new BasicResultMap(delegate.headMap(entry.getLongKey()))
                    .scoreMap()
                    .long2DoubleEntrySet();
        }

        @Override
        public ObjectSortedSet<Long2DoubleMap.Entry> tailSet(Long2DoubleMap.Entry entry) {
            return new BasicResultMap(delegate.tailMap(entry.getLongKey()))
                    .scoreMap()
                    .long2DoubleEntrySet();
        }

        @Override
        public Comparator<? super Long2DoubleMap.Entry> comparator() {
            // TODO Implement this method
            return null;
        }

        @Override
        public Long2DoubleMap.Entry first() {
            if (delegate.isEmpty()) {
                throw new NoSuchElementException();
            } else {
                long k = delegate.firstLongKey();
                return new AbstractLong2DoubleMap.BasicEntry(k, getScore(k));
            }
        }

        @Override
        public Long2DoubleMap.Entry last() {
            if (delegate.isEmpty()) {
                throw new NoSuchElementException();
            } else {
                long k = delegate.lastLongKey();
                return new AbstractLong2DoubleMap.BasicEntry(k, getScore(k));
            }
        }
    }

    private class ScoreMapEntryIter extends AbstractObjectBidirectionalIterator<Long2DoubleMap.Entry> {
        BidirectionalIterator<Result> results = delegate.iterator();

        @Override
        public Long2DoubleMap.Entry previous() {
            Result r = results.previous();
            return new AbstractLong2DoubleMap.BasicEntry(r.getId(), r.getScore());
        }

        @Override
        public boolean hasPrevious() {
            return results.hasPrevious();
        }

        public boolean hasNext() {
            return results.hasNext();
        }

        @Override
        public Long2DoubleMap.Entry next() {
            Result r = results.next();
            return new AbstractLong2DoubleMap.BasicEntry(r.getId(), r.getScore());
        }
    }

    private class ScoreMapEntryFastIter extends AbstractObjectBidirectionalIterator<Long2DoubleMap.Entry> {
        BidirectionalIterator<Result> results = delegate.iterator();
        ResultScoreEntry entry = new ResultScoreEntry(null);

        @Override
        public Long2DoubleMap.Entry previous() {
            entry.result = results.previous();
            return entry;
        }

        @Override
        public boolean hasPrevious() {
            return results.hasPrevious();
        }

        public boolean hasNext() {
            return results.hasNext();
        }

        @Override
        public Long2DoubleMap.Entry next() {
            entry.result = results.next();
            return entry;
        }
    }

    private static class ResultScoreEntry implements Long2DoubleMap.Entry {
        private Result result;

        public ResultScoreEntry(Result r) {
            result = r;
        }

        @Override
        public long getLongKey() {
            return result.getId();
        }

        @Override
        public double setValue(double v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getDoubleValue() {
            return result.getScore();
        }

        @Override
        public Long getKey() {
            return result.getId();
        }

        @Override
        public Double getValue() {
            return result.getScore();
        }

        @Override
        public Double setValue(Double value) {
            throw new UnsupportedOperationException();
        }
    }
}

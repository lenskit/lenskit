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
package org.lenskit.results;

import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;
import org.lenskit.api.Result;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.concurrent.Immutable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

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
        delegate = new KeyedObjectMap<>(objs, Results.keyExtractor());
    }

    @Override
    public Long2DoubleMap scoreMap() {
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

    private class ScoreMapImpl extends AbstractLong2DoubleMap {
        private static final long serialVersionUID = 1L;

        @Override
        public ObjectSet<Entry> long2DoubleEntrySet() {
            return new AbstractObjectSet<Entry>() {
                @Override
                public ObjectIterator<Entry> iterator() {
                    return new AbstractObjectIterator<Entry>() {
                        Iterator<Result> results = delegate.values().iterator();

                        public boolean hasNext() {
                            return results.hasNext();
                        }

                        @Override
                        public Entry next() {
                            Result r = results.next();
                            return new BasicEntry(r.getId(), r.getScore());
                        }
                    };
                }

                @Override
                public int size() {
                    return delegate.size();
                }
            };
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
        public int size() {
            return delegate.size();
        }
    }
}

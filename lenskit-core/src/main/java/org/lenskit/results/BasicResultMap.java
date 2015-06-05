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

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import org.lenskit.api.Result;

import javax.annotation.concurrent.Immutable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Standard result map implementation.
 */
@Immutable
public class BasicResultMap extends AbstractLong2ObjectMap<Result> implements LenskitResultMap {
    private final Long2ObjectMap<Result> delegate;

    /**
     * Create a new result map from a collection of results.
     * @param objs The results.
     */
    public BasicResultMap(Collection<? extends Result> objs) {
        delegate = new Long2ObjectLinkedOpenHashMap<>();
        for (Result r: objs) {
            delegate.put(r.getId(), r);
        }
    }

    @Override
    public Long2DoubleMap scoreMap() {
        return new ScoreMapImpl();
    }

    @Override
    public Iterator<Result> iterator() {
        return Iterators.unmodifiableIterator(delegate.values().iterator());
    }

    @Override
    public ObjectSet<Entry<Result>> long2ObjectEntrySet() {
        return ObjectSets.unmodifiable(delegate.long2ObjectEntrySet());
    }

    @Override
    public Result get(long l) {
        return delegate.get(l);
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public Set<Result> resultSet() {
        return new ResultSetImpl();
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

    private class ResultSetImpl extends AbstractSet<Result> {
        @Override
        public Iterator<Result> iterator() {
            return Iterators.unmodifiableIterator(delegate.values().iterator());
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Result) {
                Result or = (Result) o;
                Result found = delegate.get(or.getId());
                return or.equals(found);
            } else {
                return false;
            }
        }

        @Override
        public int size() {
            return delegate.size();
        }
    }
}

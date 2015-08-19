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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.longs.AbstractLong2DoubleMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("squid:S1948") // non-serializable field; wrapper serializable iff wrapped is
class Long2DoubleMapWrapper extends AbstractLong2DoubleMap {
    private static final long serialVersionUID = 1L;

    private final Map<Long, Double> map;

    public Long2DoubleMapWrapper(Map<Long, Double> map) {
        this.map = map;
    }

    @Override
    public ObjectSet<Entry> long2DoubleEntrySet() {
        return new EntrySet();
    }

    @Override
    public double get(long l) {
        Double d = map.get(l);
        if (d == null) {
            return defaultReturnValue();
        } else {
            return d;
        }
    }

    @Override
    public double put(long key, double value) {
        Double rv = map.put(key, value);
        return rv != null ? rv : defaultReturnValue();
    }

    @Override
    public Double put(Long ok, Double ov) {
        return map.put(ok, ov);
    }

    @Override
    public int size() {
        return map.size();
    }

    /**
     * Entry set implementation.
     */
    private class EntrySet extends AbstractObjectSet<Entry> {
        Set<Map.Entry<Long,Double>> set = map.entrySet();

        @Override
        public ObjectIterator<Entry> iterator() {
            return new EntryIterator(set.iterator());
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean contains(Object o) {
            return set.contains(this);
        }
    }

    /**
     * Iterator for entries.
     */
    private static class EntryIterator extends AbstractObjectIterator<Entry> {
        Iterator<Map.Entry<Long,Double>> iter;

        public EntryIterator(Iterator<Map.Entry<Long,Double>> it) {
            iter = it;
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        @Override
        public Entry next() {
            Map.Entry<Long,Double> e = iter.next();
            return new BasicEntry(e.getKey(), e.getValue());
        }
    }
}

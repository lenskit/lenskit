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
import it.unimi.dsi.fastutil.longs.AbstractLong2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.lenskit.api.Result;

import java.util.Iterator;
import java.util.Set;

public class BasicResultMap extends AbstractLong2ObjectMap<Result> implements LenskitResultMap {
    private final Long2ObjectMap<Result> delegate;

    public BasicResultMap(Long2ObjectMap<Result> map) {
        // TODO find way to avoid copy when map is already immutable
        this(map, true);
    }

    BasicResultMap(Long2ObjectMap<Result> map, boolean copy) {
        if (copy) {
            delegate = new Long2ObjectLinkedOpenHashMap<>(map);
        } else {
            delegate = map;
        }
    }

    @Override
    public Long2DoubleMap scoreMap() {
        return null;
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
        return null;
    }

    @Override
    public double getScore(long id) {
        return 0;
    }
}

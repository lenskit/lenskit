/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.data;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.AbstractList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import com.google.common.base.Function;

/**
 * An abstract implementation of {@link UserHistory} to provide default
 * implementations of convenience methods.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public abstract class AbstractUserHistory<E extends Event> extends AbstractList<E> implements UserHistory<E> {
    @SuppressWarnings("rawtypes")
    private final Map<Function, Object> memTable = new ConcurrentHashMap<Function, Object>();

    @Override
    public LongSet itemSet() {
        LongSet items = new LongOpenHashSet();
        for (Event e: this) {
            items.add(e.getItemId());
        }
        return items;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T memoize(Function<? super UserHistory<E>, ? extends T> func) {
         if (!memTable.containsKey(func)) {
             memTable.put(func, func.apply(this));
         }
         return (T) memTable.get(func);
    }
}

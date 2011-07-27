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
import java.util.ArrayList;
import java.util.List;

import org.grouplens.lenskit.data.event.Event;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * An abstract implementation of {@link UserHistory} to provide default
 * implementations of convenience methods.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public abstract class AbstractUserHistory<E extends Event> extends AbstractList<E> implements UserHistory<E> {
    
    /**
     * Filter into a new {@link BasicUserHistory} backed by an {@link ArrayList}.
     */@Override
    public <T extends E> UserHistory<T> filter(Class<T> type) {
        List<T> events = Lists.newArrayList(Iterables.filter(this, type));
        return new BasicUserHistory<T>(getUserId(), events);
    }
    
    /**
     * Filter into a new {@link BasicUserHistory} backed by an {@link ArrayList}.
     */
    @Override
    public UserHistory<E> filter(Predicate<? super E> pred) {
        List<E> events = Lists.newArrayList(Iterables.filter(this, pred));
        return new BasicUserHistory<E>(getUserId(), events);
    }
    
    @Override
    public LongSet itemSet() {
        LongSet items = new LongOpenHashSet();
        for (Event e: this) {
            items.add(e.getItemId());
        }
        return items;
    }
}

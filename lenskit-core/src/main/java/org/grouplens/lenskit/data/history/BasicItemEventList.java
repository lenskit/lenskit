/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.data.history;

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.data.event.Event;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * List-backed item event collection.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BasicItemEventList<E extends Event> implements ItemEventCollection<E>, List<E> {
    private final long itemId;
    private final List<E> events;

    /**
     * Construct an item event list.
     * @param item The item ID.
     * @param evts The events (will be defensively copied with {@link ImmutableList#copyOf(Collection)}).
     */
    public BasicItemEventList(long item, Collection<? extends E> evts) {
        itemId = item;
        events = ImmutableList.copyOf(evts);
    }

    @Override
    public long getItemId() {
        return itemId;
    }

    @Override
    public int size() {
        return events.size();
    }

    @Override
    public boolean isEmpty() {
        return events.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return events.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return events.iterator();
    }

    @Override
    public Object[] toArray() {
        return events.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return events.toArray(a);
    }

    public boolean add(E e) {
        return events.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return events.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return events.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return events.addAll(c);
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        return events.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return events.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return events.retainAll(c);
    }

    @Override
    public void clear() {
        events.clear();
    }

    @Override
    public boolean equals(Object o) {
        return events.equals(o);
    }

    @Override
    public int hashCode() {
        return events.hashCode();
    }

    @Override
    public E get(int index) {
        return events.get(index);
    }

    public E set(int index, E element) {
        return events.set(index, element);
    }

    public void add(int index, E element) {
        events.add(index, element);
    }

    @Override
    public E remove(int index) {
        return events.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return events.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return events.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return events.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        return events.listIterator(index);
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return events.subList(fromIndex, toIndex);
    }
}

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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.UnmodifiableListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * Basic list-based implementation of a result list.
 */
public class BasicResultList<E extends Result> extends AbstractList<E> implements LenskitResultList<E> {
    private final ImmutableList<E> results;
    private final IdList idList = new IdList();

    /**
     * Create a new result list from a list of results.
     * @param rss The result list.
     */
    public BasicResultList(List<? extends E> rss) {
        results = ImmutableList.copyOf(rss);
    }

    @Override
    public E get(int index) {
        return results.get(index);
    }

    @Override
    public int size() {
        return results.size();
    }

    @Override
    public UnmodifiableIterator<E> iterator() {
        return results.iterator();
    }

    @Override
    public UnmodifiableListIterator<E> listIterator() {
        return results.listIterator();
    }

    @Override
    public UnmodifiableListIterator<E> listIterator(int index) {
        return results.listIterator(index);
    }

    @Override
    public int indexOf(Object object) {
        return results.indexOf(object);
    }

    @Override
    public int lastIndexOf(Object object) {
        return results.lastIndexOf(object);
    }

    @Override
    public boolean contains(Object object) {
        return results.contains(object);
    }

    @Override
    public ImmutableList<E> subList(int fromIndex, int toIndex) {
        return results.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ResultList) {
            return results.equals(obj);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return results.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return results.isEmpty();
    }

    @Override
    public Object[] toArray() {
        return results.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return results.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return results.containsAll(c);
    }

    @Override
    public LongList idList() {
        return idList;
    }

    private class IdList extends AbstractLongList {
        @Override
        public int size() {
            return results.size();
        }

        @Override
        public long getLong(int i) {
            return results.get(i).getId();
        }
    }
}

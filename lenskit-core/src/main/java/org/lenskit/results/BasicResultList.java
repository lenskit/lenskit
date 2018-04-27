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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.UnmodifiableListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.api.Result;
import org.lenskit.api.ResultList;

import net.jcip.annotations.Immutable;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

/**
 * Basic list-based implementation of a result list.
 */
@Immutable
public class BasicResultList extends AbstractList<Result> implements LenskitResultList {
    private final ImmutableList<Result> results;
    private final IdList idList = new IdList();

    /**
     * Create a new result list from a list of results.
     * @param rss The result list.
     */
    public BasicResultList(List<? extends Result> rss) {
        results = ImmutableList.copyOf(rss);
    }

    @Override
    public Result get(int index) {
        return results.get(index);
    }

    @Override
    public int size() {
        return results.size();
    }

    @Override
    public UnmodifiableIterator<Result> iterator() {
        return results.iterator();
    }

    @Override
    public UnmodifiableListIterator<Result> listIterator() {
        return results.listIterator();
    }

    @Override
    public UnmodifiableListIterator<Result> listIterator(int index) {
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
    public ImmutableList<Result> subList(int fromIndex, int toIndex) {
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

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

package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * An object collection wrapper for a collection.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ObjectCollectionWrapper<E> implements ObjectCollection<E>, Serializable {
    private static final long serialVersionUID = 1L;

    private Collection<E> delegate;

    public ObjectCollectionWrapper(Collection<E> objects) {
        delegate = objects;
    }

    @Override
    public ObjectIterator<E> iterator() {
        return ObjectIterators.asObjectIterator(delegate.iterator());
    }

    @Override
    public ObjectIterator<E> objectIterator() {
        return iterator();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        if (size() <= a.length) {
            ObjectIterators.unwrap(iterator(), a);
            return a;
        } else {
            return delegate.toArray(a);
        }
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public boolean add(E e) {
        return delegate.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return delegate.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof List || obj instanceof Set) {
            return false;
        } else {
            return delegate.equals(obj);
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}

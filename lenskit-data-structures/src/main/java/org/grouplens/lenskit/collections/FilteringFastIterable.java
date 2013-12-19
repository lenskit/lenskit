package org.grouplens.lenskit.collections;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

import java.util.Iterator;

/**
 * Implementation of {@link CollectionUtils#fastFilterAndLimit(Iterable, Predicate, int)}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class FilteringFastIterable<E> implements FastIterable<E> {
    private final Iterable<E> delegate;
    private final Predicate<? super E> predicate;
    private final int limit;

    public FilteringFastIterable(Iterable<E> iter, Predicate<? super E> pred, int limit) {
        delegate = iter;
        predicate = pred;
        this.limit = limit;
    }

    @Override
    public Iterator<E> fastIterator() {
        if (delegate instanceof FastIterable) {
            // REVIEW Is Iterators.filter really reasonable?
            Iterator<E> iter = Iterators.filter(((FastIterable<E>) delegate).fastIterator(), predicate);
            return limit(iter);
        } else {
            return iterator();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return limit(Iterators.filter(delegate.iterator(), predicate));
    }

    private Iterator<E> limit(Iterator<E> iter) {
        if (limit >= 0) {
            return Iterators.limit(iter, limit);
        } else {
            return iter;
        }
    }
}

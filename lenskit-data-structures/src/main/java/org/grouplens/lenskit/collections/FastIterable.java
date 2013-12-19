package org.grouplens.lenskit.collections;

import java.util.Iterator;

/**
 * Iterable supporting fast iteration.
 *
 * <p>A fast iterable is an iterable that may support <em>fast iteration</em>
 * — iteration where the same object is returned each time, having been mutated
 * to represent the next state.  It can save greatly in object allocation
 * overhead in some circumstances.  Using a fast iterator is only possible if
 * the looping code doesn't allow objects returned by the iterator to escape the
 * loop (e.g. it doesn't save them away in other objects or something), but many
 * loops only observe the object and then discard it before the next iteration.
 * Those loops benefit from fast iterators.
 *
 * @param <E> The type of value in the fast collection.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 * @see CollectionUtils#fast(Iterable)
 */
public interface FastIterable<E> extends Iterable<E> {
    /**
     * Return a fast iterator.  The iterator may not actually be fast; if the
     * underlying structure does not support fast iteration, the iterator may
     * return distinct objects every time.  However, that is usually in cases
     * where the underlying collection is storing distinct objects anyway so no
     * overhead is introduced.
     *
     * @return An iterator that may not return distinct objects.
     */
    Iterator<E> fastIterator();
}

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
package org.grouplens.lenskit.collections;

import java.util.Collection;
import java.util.Iterator;

/**
 * Collection supporting fast iteration.
 *
 * <p>A fast collection is a collection that may support <em>fast iteration</em>
 * — iteration where the same object is returned each time, having been mutated
 * to represent the next state.  It can save greatly in object allocation
 * overhead in some circumstances.  Using a fast iterator is only possible if
 * the looping code doesn't allow objects returned by the iterator to escape the
 * loop (e.g. it doesn't save them away in other objects or something), but many
 * loops only observe the object and then discard it before the next iteration.
 * Those loops benefit from fast iterators.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface FastCollection<E> extends Collection<E> {
    /**
     * Return a fast iterator.  The iterator may not actually be fast; if the
     * underlying structure does not support fast iteration, the iterator may
     * return distinct objects every time.  However, that is usually in cases
     * where the underlying collection is storing distinct objects anyway so no
     * overhead is introduced.
     * @return An iterator that may not return distinct objects.
     */
    Iterator<E> fastIterator();

    /**
     * Make a fast iterable from the collection.  This method returns an iterable
     * whose iterator may be fast.  It allows fast iterators to be used in
     * for-each loops.
     * @return An {@link Iterable} whose {@link Iterable#iterator()} method calls
     * {@link #fastIterator()}.
     */
    Iterable<E> fast();
}

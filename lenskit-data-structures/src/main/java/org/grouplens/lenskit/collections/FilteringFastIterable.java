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
        return iterator();
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

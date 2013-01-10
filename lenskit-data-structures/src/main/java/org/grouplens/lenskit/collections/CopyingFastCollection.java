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
package org.grouplens.lenskit.collections;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * Abstract fast collection that implements {@link #iterator()} in terms of
 * {@link #fastIterator()} and {@link #copy(E)}.
 *
 * @author Michael Ekstrand
 * @since 1.1
 */
public abstract class CopyingFastCollection<E> extends AbstractCollection<E> implements FastCollection<E> {
    private final Function<E, E> copyFunction = new Function<E, E>() {
        @Override
        public E apply(@Nullable E input) {
            return copy(input);
        }
    };

    /**
     * Copy an element of the collection.
     *
     * @param elt The element to copy.
     * @return A copy of {@var elt}
     */
    protected abstract E copy(E elt);

    @Override
    public Iterator<E> iterator() {
        return Iterators.transform(fastIterator(), copyFunction);
    }
}

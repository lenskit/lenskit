/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.io;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link ObjectStream} that simply wraps an iterator.
 *
 * @param <T> The stream's element type.
 */
class IteratorObjectStream<T> extends AbstractObjectStream<T> {
    private final Collection<? extends T> collection;
    private Iterator<? extends T> iterator;
    private int ndone = 0;

    IteratorObjectStream(@Nonnull Collection<? extends T> col) {
        collection = col;
        iterator = col.iterator();
    }

    /**
     * Construct a new iterator stream.
     * @param iter The iterator.
     */
    IteratorObjectStream(@Nonnull Iterator<? extends T> iter) {
        Preconditions.checkNotNull(iter, "stream iterator");
        iterator = iter;
        collection = null;
    }

    List<T> getList() {
        if (collection != null && ndone == 0) {
            return ImmutableList.copyOf(collection);
        } else {
            return null;
        }
    }

    @Override
    public T readObject() {
        Preconditions.checkState(iterator != null, "stream has been closed");
        if (iterator.hasNext()) {
            T obj = iterator.next();
            ndone += 1;
            if (obj == null) {
                throw new NullPointerException("object stream iterator cannot contain null");
            }
            return obj;
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return Iterators.unmodifiableIterator((Iterator<T>) iterator);
    }

    @Override
    public void close() {
        super.close();
        iterator = null;
    }
}

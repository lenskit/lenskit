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
package org.lenskit.util.io;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Implementation of concatenated streams.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SequencedObjectStream<T> extends AbstractObjectStream<T> {
    private final Iterator<? extends ObjectStream<? extends T>> streamIter;
    @Nullable
    private ObjectStream<? extends T> current;

    public SequencedObjectStream(Iterable<? extends ObjectStream<? extends T>> streams) {
        streamIter = streams.iterator();
        current = Iterators.getNext(streamIter, null);
    }

    @Override
    public T readObject() {
        T obj = null;
        while (current != null) {
            obj = current.readObject();
            if (obj == null) {
                current = Iterators.getNext(streamIter, null);
            } else {
                break;
            }
        }

        return obj;
    }

    @Override
    public void close() {
        Throwable error = null;
        if (current != null) {
            try {
                current.close();
            } catch (Throwable th) { // NOSONAR We are managing errors
                error = th;
            }
            current = null;
        }
        while (streamIter.hasNext()) {
            ObjectStream<? extends T> cur = streamIter.next();
            try {
                cur.close();
            } catch (Throwable th) { // NOSONAR We are managing errors
                if (error == null) {
                    error = th;
                } else {
                    error.addSuppressed(th);
                }
            }
        }
        if (error != null) {
            throw Throwables.propagate(error);
        }
    }
}

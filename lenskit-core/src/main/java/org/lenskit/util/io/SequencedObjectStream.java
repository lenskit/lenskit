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

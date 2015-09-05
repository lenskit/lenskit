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

import com.google.common.base.Function;

import javax.annotation.WillCloseWhenClosed;

/**
 * Implementation of transformed streams.
 *
 * @param <S> The element type of the wrapped stream.
 * @param <T> The element type of the stream.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see ObjectStreams#transform(ObjectStream, Function)
 */
class TransformedObjectStream<S, T> extends AbstractObjectStream<T> {
    private final ObjectStream<S> delegate;
    private final Function<? super S, ? extends T> function;

    /**
     * Construct a transformed stream.
     *
     * @param cur The underlying stream.
     * @param fun The transformation function.
     */
    public TransformedObjectStream(@WillCloseWhenClosed ObjectStream<S> cur, Function<? super S, ? extends T> fun) {
        delegate = cur;
        function = fun;
    }

    @Override
    public T readObject() {
        S obj = delegate.readObject();
        if (obj != null) {
            T res = function.apply(obj);
            if (res == null) {
                throw new NullPointerException("stream transformer mapped " + obj + " to null");
            }
            return res;
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        delegate.close();
    }
}

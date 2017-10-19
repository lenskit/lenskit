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

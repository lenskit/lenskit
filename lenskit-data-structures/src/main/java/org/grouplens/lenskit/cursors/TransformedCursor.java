/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.cursors;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;

import com.google.common.base.Function;

/**
 * Implementation of transformed cursors.
 *
 * @param <S> The element type of the wrapped cursor.
 * @param <T> The element type of the cursor.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @see Cursors#transform(Cursor, Function)
 */
class TransformedCursor<S, T> extends AbstractCursor<T> {
    private final Cursor<S> cursor;
    private final Function<? super S, ? extends T> function;

    /**
     * Construct a transformed cursor.
     *
     * @param cur The underlying cursor.
     * @param fun The transformation function.
     */
    public TransformedCursor(@WillCloseWhenClosed Cursor<S> cur, Function<? super S, ? extends T> fun) {
        super(cur.getRowCount());
        cursor = cur;
        function = fun;
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    public boolean hasNext() {
        return cursor.hasNext();
    }

    @Nonnull
    @Override
    public T next() {
        return function.apply(cursor.next());
    }
}

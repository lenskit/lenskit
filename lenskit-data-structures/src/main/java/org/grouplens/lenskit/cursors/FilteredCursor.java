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
package org.grouplens.lenskit.cursors;

import javax.annotation.WillCloseWhenClosed;

import com.google.common.base.Predicate;

/**
 * A wrapper cursor that filters its underlying cursor.
 * @param <T> The cursor's element type.
 */
class FilteredCursor<T> extends AbstractPollingCursor<T> {
    private final Cursor<T> cursor;
    private final Predicate<? super T> filter;

    /**
     * Construct a new filtered cursor.
     * @param cur The underlying cursor.
     * @param filt The filter.
     */
    public FilteredCursor(@WillCloseWhenClosed Cursor<T> cur, Predicate<? super T> filt) {
        super();
        cursor = cur;
        filter = filt;
    }

    @Override
    public void close() {
        cursor.close();
    }

    @Override
    protected T poll() {
        while (cursor.hasNext()) {
            final T next = cursor.next();
            if (filter.apply(next)) {
                return next;
            }
        }

        // Reached the end of the base cursor, so return null
        return null;
    }
}

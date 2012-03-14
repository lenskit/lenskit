/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class FilteredCursor<T> extends AbstractPollingCursor<T> {
	private final Cursor<T> cursor;
	private final Predicate<? super T> filter;
	
	public FilteredCursor(@WillCloseWhenClosed Cursor<T> cursor, Predicate<? super T> filter) {
	    super();
		this.cursor = cursor;
		this.filter = filter;
	}
	
	@Override
	public void close() {
		cursor.close();
	}

    @Override
    protected T poll() {
        while(cursor.hasNext()) {
            T next = cursor.next();
            if (filter.apply(next))
                return next;
        }
        
        // Reached the end of the base cursor, so return null
        return null;
    }
}

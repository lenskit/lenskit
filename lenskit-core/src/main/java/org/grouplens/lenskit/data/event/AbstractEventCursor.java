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
package org.grouplens.lenskit.data.event;

import org.grouplens.lenskit.cursors.AbstractPollingCursor;
import org.grouplens.lenskit.data.Event;

/**
 * Helper for making event cursors.
 *
 * @param <E> The type of event the cursor returns.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public abstract class AbstractEventCursor<E extends Event> extends AbstractPollingCursor<E> {
    /**
     * Construct a cursor of unknown size.
     */
    public AbstractEventCursor() {
        super();
    }

    /**
     * Construct a cursor with a known number of rows.
     *
     * @param rowCount The number of rows.
     */
    public AbstractEventCursor(int rowCount) {
        super(rowCount);
    }

    /**
     * Copy an event using {@link Event#copy()}.
     *
     * @deprecated This implementation will be removed in LensKit 2.0.
     * @param event The event to copy.
     * @return A copy of {@code event}.
     */
    @Deprecated @Override
    @SuppressWarnings("unchecked")
    protected E copy(E event) {
        return (E) event.copy();
    }
}

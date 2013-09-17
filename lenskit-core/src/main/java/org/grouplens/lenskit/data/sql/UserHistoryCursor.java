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
package org.grouplens.lenskit.data.sql;

import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.data.history.History;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Cursor that processes (user,timestamp)-sorted cursor of events and groups
 * them into user histories.
 *
 * @param <E> The event type.
 */
class UserHistoryCursor<E extends Event> extends AbstractCursor<UserHistory<E>> {
    private Cursor<? extends E> cursor;
    private E lastEvent;

    public UserHistoryCursor(@WillCloseWhenClosed Cursor<? extends E> cur) {
        cursor = cur;
        lastEvent = null;
    }

    @Override
    public void close() {
        if (cursor != null) {
            cursor.close();
        }
        cursor = null;
        lastEvent = null;
    }

    @Override
    public boolean hasNext() {
        return cursor != null && (lastEvent != null || cursor.hasNext());
    }

    @Override
    @Nonnull
    public UserHistory<E> next() {
        if (cursor == null) {
            throw new NoSuchElementException();
        }
        long uid;
        List<E> events = new ArrayList<E>();
        if (lastEvent == null) {
            lastEvent = cursor.next();
        }
        uid = lastEvent.getUserId();
        do {
            events.add(lastEvent);
            if (cursor.hasNext()) {
                lastEvent = cursor.next();
            } else {
                lastEvent = null;
            }
        } while (lastEvent != null && lastEvent.getUserId() == uid);

        return History.forUser(uid, events);
    }
}
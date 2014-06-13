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
package org.grouplens.lenskit.data.sql;

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.GroupingCursor;
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
class UserHistoryCursor<E extends Event> extends GroupingCursor<UserHistory<E>,E> {
    private ImmutableList.Builder<E> builder;
    private long userId;

    public UserHistoryCursor(@WillCloseWhenClosed Cursor<? extends E> cur) {
        super(cur);
    }

    @Override
    protected void clearGroup() {
        builder = null;
    }

    @Override
    protected boolean handleItem(E event) {
        if (builder == null) {
            userId = event.getUserId();
            builder = ImmutableList.builder();
        }

         if (userId == event.getUserId()) {
            builder.add(event);
            return true;
         } else {
             return false;
         }
    }

    @Nonnull
    @Override
    protected UserHistory<E> finishGroup() {
        List<E> events = builder.build();
        builder = null;
        return History.forUser(userId, events);
    }
}

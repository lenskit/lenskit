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
package org.grouplens.lenskit.data.sql;

import com.google.common.collect.ImmutableList;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.GroupingCursor;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.ItemEventCollection;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.util.List;

/**
 * Cursor that processes (user,timestamp)-sorted cursor of events and groups
 * them into user histories.
 *
 * @param <E> The event type.
 */
class ItemCollectionCursor<E extends Event> extends GroupingCursor<ItemEventCollection<E>,E> {
    private ImmutableList.Builder<E> builder;
    private long itemId;

    public ItemCollectionCursor(@WillCloseWhenClosed Cursor<? extends E> cur) {
        super(cur);
    }

    @Override
    protected void clearGroup() {
        builder = null;
    }

    @Override
    protected boolean handleItem(E event) {
        if (builder == null) {
            itemId = event.getItemId();
            builder = ImmutableList.builder();
        }

        if (itemId == event.getItemId()) {
            builder.add(event);
            return true;
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    protected ItemEventCollection<E> finishGroup() {
        List<E> events = builder.build();
        builder = null;
        return History.forItem(itemId, events);
    }
}

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
/**
 *
 */
package org.grouplens.lenskit.data.dao;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;

import org.grouplens.lenskit.cursors.AbstractCursor;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.cursors.LongCursor;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.history.BasicUserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;

/**
 * Abstract implementation of {@link DataAccessObject}, delegating
 * to a few core methods.  It also handles thread-local session management.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public abstract class AbstractDataAccessObject implements DataAccessObject {
    protected final Logger logger;

    protected AbstractDataAccessObject() {
        logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Delegate to {@link #getEvents(Class, SortOrder)}, filtering with
     * {@link Event} as the type.
     */
    @Override
    public Cursor<? extends Event> getEvents(SortOrder order) {
        return getEvents(Event.class, order);
    }

    /**
     * Delegate to {@link #getEvents(Class, SortOrder)}.
     */
    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type) {
        return getEvents(type, SortOrder.ANY);
    }

    /**
     * Implement {@link DataAccessObject#getEvents(Class,SortOrder)} by sorting
     * and filtering output of {@link #getEvents()}.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) {
        Comparator<Event> comp = getComparator(order);

        Cursor<E> cursor;
        if (type.equals(Event.class))
            cursor = (Cursor<E>) getEvents();
        else
            cursor = Cursors.filter(getEvents(), type);
        if (comp == null) {
            return cursor;
        } else {
            return Cursors.sort(cursor, comp);
        }
    }

    /**
     * Get a comparator for a particular sort order.
     * @param order The sort order.
     * @return A comparator over events implementing the specified sort order.
     */
    protected static Comparator<Event> getComparator(SortOrder order) {
        switch (order) {
        case ANY:
            return null;
        case TIMESTAMP:
            return Events.TIMESTAMP_COMPARATOR;
        case USER:
            return Events.USER_TIME_COMPARATOR;
        case ITEM:
            return Events.ITEM_TIME_COMPARATOR;
        default:
            throw new IllegalArgumentException();
        }
    }

    /**
     * Delegate to {@link #getUserHistories(Class)}.
     */
    @Override
    public Cursor<UserHistory<Event>> getUserHistories() {
        return getUserHistories(Event.class);
    }

    /**
     * Implement {@link DataAccessObject#getUserHistories(Class)} by
     * processing the output of {@link #getEvents(Class,SortOrder)} sorted by user.
     */
    @Override
    public <E extends Event> Cursor<UserHistory<E>> getUserHistories(Class<E> type) {
        Cursor<E> events = getEvents(type, SortOrder.USER);
        try {
            return new UserHistoryCursor<E>(events);
        } catch (RuntimeException e) {
            events.close();
            throw e;
        }
    }

    /**
     * Implement {@link DataAccessObject#getUserEvents(long)} by delegating to
     * {@link #getUserEvents(long, Class)}.
     */
    @Override
    public Cursor<? extends Event> getUserEvents(long userId) {
        return getUserEvents(userId, Event.class);
    }

    /**
     * Implement {@link DataAccessObject#getUserEvents(long, Class)} by
     * filtering the output of {@link #getEvents(Class,SortOrder)}.
     */
    @Override
    public <E extends Event> Cursor<E> getUserEvents(final long userId, Class<E> type) {
        Predicate<E> pred = new Predicate<E>() {
            @Override
            public boolean apply(E event) {
                return event.getUserId() == userId;
            }
        };
        return Cursors.filter(getEvents(type, SortOrder.TIMESTAMP), pred);
    }

    /**
     * Implement {@link DataAccessObject#getUserHistory(long)} by delegating
     * to {@link #getUserEvents(long)}.
     */
    @Override
    public UserHistory<Event> getUserHistory(long user) {
        return new BasicUserHistory<Event>(user, Cursors.makeList(getUserEvents(user)));
    }

    /**
     * Implement {@link DataAccessObject#getUserHistory(long,Class)} by delegating
     * to {@link #getUserEvents(long,Class)}.
     */
    @Override
    public <E extends Event> UserHistory<E> getUserHistory(long user, Class<E> type) {
        return new BasicUserHistory<E>(user, Cursors.makeList(getUserEvents(user, type)));
    }

    /**
     * Implement {@link DataAccessObject#getItemEvents(long)} by delegating
     * to {@link #getItemEvents(long, Class)}.
     */
    @Override
    public Cursor<? extends Event> getItemEvents(long itemId) {
        return getItemEvents(itemId, Event.class);
    }

    /**
     * Implement {@link DataAccessObject#getItemEvents(long, Class)}
     * by filtering the output of {@link #getEvents(Class,SortOrder)}.
     */
    @Override
    public <E extends Event> Cursor<E> getItemEvents(final long itemId, Class<E> type) {
        Predicate<E> pred = new Predicate<E>() {
            @Override
            public boolean apply(E event) {
                return event.getItemId() == itemId;
            }
        };
        return Cursors.filter(getEvents(type, SortOrder.USER), pred);
    }

    private LongSet getItemSet() {
        LongSet items = null;

        Cursor<? extends Event> ratings = getEvents();
        try {
            items = new LongOpenHashSet();
            for (Event r: ratings) {
                items.add(r.getItemId());
            }
        } finally {
            ratings.close();
        }

        return items;
    }

    /**
     * Implement {@link DataAccessObject#getItems()} by processing the output
     * of {@link #getEvents(Class)}.
     */
    @Override
    public LongCursor getItems() {
        return Cursors.wrap(getItemSet());
    }

    @Override
    public int getItemCount() {
        return getItemSet().size();
    }

    private LongSet getUserSet() {
        LongSet users;

        Cursor<? extends Event> events = getEvents();
        try {
            users = new LongOpenHashSet();
            for (Event evt: events) {
                users.add(evt.getUserId());
            }
        } finally {
            events.close();
        }

        return users;
    }

    /**
     * Implement {@link DataAccessObject#getUsers()} by processing the output
     * of {@link #getEvents(Class)}.
     */
    @Override
    public LongCursor getUsers() {
        return Cursors.wrap(getUserSet());
    }

    @Override
    public int getUserCount() {
        return getUserSet().size();
    }

    /**
     * Cursor that processes (user,timestamp)-sorted cursor of events and groups
     * them into user histories.
     *
     * @param <E> The event type.
     */
    static class UserHistoryCursor<E extends Event> extends AbstractCursor<UserHistory<E>> {
        private Cursor<? extends E> cursor;
        private E lastEvent;

        public UserHistoryCursor(@WillCloseWhenClosed Cursor<? extends E> cursor) {
            this.cursor = cursor;
            lastEvent = null;
        }

        @Override
        public void close() {
            if (cursor != null)
                cursor.close();
            cursor = null;
            lastEvent = null;
        }

        @Override
        public boolean hasNext() {
            return cursor != null && (lastEvent != null || cursor.hasNext());
        }

        @Override @Nonnull
        public UserHistory<E> next() {
            if (cursor == null) throw new NoSuchElementException();
            long uid;
            List<E> events = new ArrayList<E>();
            if (lastEvent == null)
                lastEvent = cursor.next();
            uid = lastEvent.getUserId();
            do {
                events.add(lastEvent);
                if (cursor.hasNext())
                    lastEvent = cursor.next();
                else
                    lastEvent = null;
            } while (lastEvent != null && lastEvent.getUserId() == uid);

            return new BasicUserHistory<E>(uid, events);
        }
    }
}

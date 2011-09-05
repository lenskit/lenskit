/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import static com.google.common.collect.Iterables.filter;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Cursors2;
import org.grouplens.lenskit.data.LongCursor;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Events;
import org.grouplens.lenskit.data.history.BasicUserHistory;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.util.TypeUtils;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * Data source backed by a collection of events.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class EventCollectionDAO extends AbstractDataAccessObject {
    /**
     * Factory for creating event collection DAOs.  It assumes that the collection
     * is not modified by other code, so a singleton DAO is created and returned
     * for both {@link #create()} and {@link #snapshot()}.
     * @author Michael Ekstrand <ekstrand@cs.umn.edu>
     *
     */
    @ThreadSafe
    public static class Factory implements DAOFactory {
        private final Collection<? extends Event> events;
        private transient volatile  EventCollectionDAO singleton;

        public Factory(Collection<? extends Event> ratings) {
            this.events = ratings;
        }

        @Override
        public synchronized EventCollectionDAO create() {
            if (singleton == null) {
                singleton = new EventCollectionDAO(events);
                singleton.requireItemCache();
                singleton.requireUserCache();
            }

            return singleton;
        }

        @Override
        public EventCollectionDAO snapshot() {
            return create();
        }
    }

    private Collection<? extends Event> ratings;
    private Set<Class<? extends Event>> types;
    private Long2ObjectMap<UserHistory<Event>> users;
    private Long2ObjectMap<ArrayList<Event>> items;

    /**
     * Construct a new data source from a collection of events.
     * @param events The events to use.
     */
    public EventCollectionDAO(Collection<? extends Event> events) {
        logger.debug("Creating event collection DAO for {} events", events.size());
        this.ratings = events;

        // Scan for the types in the data source. Since this scans
        types = TypeUtils.findTypes(events, Event.class);
    }

    private synchronized void requireUserCache() {
        if (users == null) {
            logger.debug("Caching user histories");
            Long2ObjectMap<ArrayList<Event>> ratingCs =
                new Long2ObjectOpenHashMap<ArrayList<Event>>();
            for (Event r: ratings) {
                final long uid = r.getUserId();
                ArrayList<Event> userRatings = ratingCs.get(uid);
                if (userRatings == null) {
                    userRatings = new ArrayList<Event>(20);
                    ratingCs.put(uid, userRatings);
                }
                userRatings.add(r);
            }
            users = new Long2ObjectOpenHashMap<UserHistory<Event>>(ratingCs.size());
            for (Long2ObjectMap.Entry<ArrayList<Event>> e: ratingCs.long2ObjectEntrySet()) {
                e.getValue().trimToSize();
                Collections.sort(e.getValue(), Events.TIMESTAMP_COMPARATOR);
                users.put(e.getLongKey(), new BasicUserHistory<Event>(e.getLongKey(), e.getValue()));
            }
        }
    }

    private synchronized void requireItemCache() {
        if (items == null) {
            logger.debug("Caching item event collections");
            items = new Long2ObjectOpenHashMap<ArrayList<Event>>();
            for (Event r: ratings) {
                final long iid = r.getItemId();
                ArrayList<Event> itemRatings = items.get(iid);
                if (itemRatings == null) {
                    itemRatings = new ArrayList<Event>(20);
                    items.put(iid, itemRatings);
                }
                itemRatings.add(r);
            }
            for (ArrayList<Event> rs: items.values()) {
                rs.trimToSize();
                Collections.sort(rs, Events.USER_TIME_COMPARATOR);
            }
        }
    }

    /**
     * Query whether there may be any events of the specified type in this DAO.
     * This does not guarantee that there are, but only
     *
     * @param type
     * @return <tt>true</tt> if the data set contains some objects which are
     *         compatible with <var>type</var>.
     */
    protected boolean containsType(Class<? extends Event> type) {
        return types.contains(type);
    }

    @Override
    public LongCursor getUsers() {
        requireUserCache();
        return Cursors2.wrap(users.keySet());
    }

    @Override
    public <E extends Event> Cursor<E> getUserEvents(long user, Class<E> type) {
        if (!containsType(type))
            return Cursors.empty();

        requireUserCache();
        Collection<? extends Event> ratings = users.get(user);
        if (ratings == null) return Cursors.empty();

        return Cursors.wrap(Iterators.filter(ratings.iterator(), type));
    }

    @Override
    public Cursor<UserHistory<Event>> getUserHistories() {
        requireUserCache();
        return Cursors.wrap(users.values().iterator());
    }

    @Override
    public <E extends Event> Cursor<UserHistory<E>> getUserHistories(final Class<E> type) {
        return Cursors.transform(getUserHistories(),
                                 new Function<UserHistory<Event>, UserHistory<E>>() {
            @Override
            public UserHistory<E> apply(UserHistory<Event> input) {
                List<E> events;
                if (containsType(type))
                    events = Lists.newArrayList(filter(input, type));
                else
                    events = Collections.emptyList();
                return new BasicUserHistory<E>(input.getUserId(), events);
            }
        });
    }

    @Override
    public <E extends Event> Cursor<E> getItemEvents(long item, Class<E> type) {
        if (!containsType(type)) return Cursors.empty();

        requireItemCache();

        List<Event> ratings = items.get(item);
        if (ratings == null) return Cursors.empty();

        return Cursors.filter(Cursors.wrap(ratings), type);
    }

    @Override
    public Cursor<Event> getEvents() {
        return Cursors.wrap(ratings);
    }

    @Override
    public void close() {
        // do nothing, there is nothing to close
    }
}

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
package org.grouplens.lenskit.data.dao;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.UserHistory;
import org.grouplens.lenskit.data.history.History;

import javax.inject.Inject;

/**
 * User event DAO that pre-loads all events from an event DAO.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class StreamingUserEventDAO implements UserEventDAO {
    private final EventDAO eventDAO;
    private transient volatile Long2ObjectMap<UserHistory<Event>> userEvents;

    @Inject
    public StreamingUserEventDAO(EventDAO dao) {
        eventDAO = dao;
    }

    private void loadEvents() {
        if (userEvents != null) {
            return;
        }

        synchronized (this) {
            if (userEvents != null) {
                return;
            }
            Long2ObjectMap<ImmutableList.Builder<Event>> table =
                    new Long2ObjectOpenHashMap<ImmutableList.Builder<Event>>();
            Cursor<Event> events = eventDAO.streamEvents();
            try {
                for (Event evt: events) {
                    final long iid = evt.getUserId();
                    ImmutableList.Builder<Event> list = table.get(iid);
                    if (list == null) {
                        list = new ImmutableList.Builder<Event>();
                        table.put(iid, list);
                    }
                    list.add(evt);
                }
            } finally {
                events.close();
            }
            Long2ObjectMap<UserHistory<Event>> result = new Long2ObjectOpenHashMap<UserHistory<Event>>(table.size());
            for (Long2ObjectMap.Entry<ImmutableList.Builder<Event>> evt: table.long2ObjectEntrySet()) {
                long user = evt.getLongKey();
                result.put(user, History.forUser(user, evt.getValue().build()));
                evt.setValue(null);
            }
            userEvents = result;
        }
    }

    @Override
    public Cursor<UserHistory<Event>> streamEventsByUser() {
        loadEvents();
        return Cursors.wrap(userEvents.values());
    }

    @Override
    public UserHistory<Event> getEventsForUser(long user) {
        loadEvents();
        return userEvents.get(user);
    }

    @Override
    public <E extends Event> UserHistory<E> getEventsForUser(long user, Class<E> type) {
        UserHistory<Event> events = getEventsForUser(user);
        if (events == null) {
            return null;
        } else {
            return events.filter(type);
        }
    }
}

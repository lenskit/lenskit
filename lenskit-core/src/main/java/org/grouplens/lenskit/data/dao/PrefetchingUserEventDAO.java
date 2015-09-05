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
package org.grouplens.lenskit.data.dao;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.lenskit.data.events.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.UserHistory;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;

/**
 * User event DAO that pre-loads all events from an event DAO.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PrefetchingUserEventDAO implements UserEventDAO, Describable {
    private final EventDAO eventDAO;
    private final Supplier<Long2ObjectMap<UserHistory<Event>>> cache;

    /**
     * A function that wraps an event DAO in a prefetching user event DAO.  If the DAO already
     * implements {@link UserEventDAO}, it is returned unwrapped.
     * @return A wrapper function to make user event DAOs from event DAOs.
     */
    public static Function<EventDAO,UserEventDAO> wrapper() {
        return WrapperFunction.INSTANCE;
    }

    private static enum WrapperFunction implements Function<EventDAO,UserEventDAO> {
        INSTANCE;

        @Nullable
        @Override
        public UserEventDAO apply(@Nullable EventDAO input) {
            if (input instanceof UserEventDAO) {
                return (UserEventDAO) input;
            } else {
                return new PrefetchingUserEventDAO(input);
            }
        }
    }

    @Inject
    public PrefetchingUserEventDAO(EventDAO dao) {
        eventDAO = dao;
        cache = Suppliers.memoize(new UserProfileScanner());
    }

    @Override
    public ObjectStream<UserHistory<Event>> streamEventsByUser() {
        return ObjectStreams.wrap(cache.get().values());
    }

    @Override
    public <E extends Event> ObjectStream<UserHistory<E>> streamEventsByUser(final Class<E> type) {
        return ObjectStreams.transform(streamEventsByUser(), new Function<UserHistory<Event>, UserHistory<E>>() {
            @Nullable
            @Override
            public UserHistory<E> apply(@Nullable UserHistory<Event> input) {
                return input == null ? null : input.filter(type);
            }
        });
    }

    @Override
    public UserHistory<Event> getEventsForUser(long user) {
        return cache.get().get(user);
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

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("daoType", "UserEvent")
              .putField("delegate", eventDAO);
    }

    private class UserProfileScanner implements Supplier<Long2ObjectMap<UserHistory<Event>>> {
        @Override
        public Long2ObjectMap<UserHistory<Event>> get() {
            Long2ObjectMap<List<Event>> table =
                    new Long2ObjectOpenHashMap<List<Event>>();
            ObjectStream<Event> events = eventDAO.streamEvents();
            try {
                for (Event evt: events) {
                    final long iid = evt.getUserId();
                    List<Event> list = table.get(iid);
                    if (list == null) {
                        list = Lists.newArrayList();
                        table.put(iid, list);
                    }
                    list.add(evt);
                }
            } finally {
                events.close();
            }
            Long2ObjectMap<UserHistory<Event>> result = new Long2ObjectOpenHashMap<UserHistory<Event>>(table.size());
            for (Long2ObjectMap.Entry<List<Event>> evt: table.long2ObjectEntrySet()) {
                long user = evt.getLongKey();
                result.put(user, History.forUser(user, evt.getValue()));
                evt.setValue(null);
            }
            return result;
        }
    }
}

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

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.History;
import org.grouplens.lenskit.data.history.ItemEventCollection;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;

/**
 * Item event DAO that pre-loads all events from an event DAO.
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class PrefetchingItemEventDAO implements ItemEventDAO {
    private final EventDAO eventDAO;
    private final Supplier<Long2ObjectMap<List<Event>>> cache;

    @Inject
    public PrefetchingItemEventDAO(EventDAO dao) {
        eventDAO = dao;
        cache = Suppliers.memoize(new ItemProfileScanner());
    }

    @Override
    public Cursor<ItemEventCollection<Event>> streamEventsByItem() {
        Long2ObjectMap<List<Event>> map = cache.get();
        return Cursors.wrap(Iterators.transform(map.entrySet().iterator(),
                                                ItemEventTransform.INSTANCE));
    }

    @Override
    public List<Event> getEventsForItem(long item) {
        return cache.get().get(item);
    }

    @Override
    public <E extends Event> List<E> getEventsForItem(long item, Class<E> type) {
        List<Event> events = getEventsForItem(item);
        if (events == null) {
            return null;
        } else {
            return ImmutableList.copyOf(Iterables.filter(events, type));
        }
    }

    @Override
    public LongSet getUsersForItem(long item) {
        List<Event> events = getEventsForItem(item);
        if (events == null) {
            return null;
        }

        LongSet users = new LongOpenHashSet();
        for (Event evt: CollectionUtils.fast(events)) {
            users.add(evt.getUserId());
        }
        return users;
    }

    private class ItemProfileScanner implements Supplier<Long2ObjectMap<List<Event>>> {
        @Override
        public Long2ObjectMap<List<Event>> get() {
            Long2ObjectMap<ImmutableList.Builder<Event>> table =
                    new Long2ObjectOpenHashMap<ImmutableList.Builder<Event>>();
            Cursor<Event> events = eventDAO.streamEvents();
            try {
                for (Event evt: events) {
                    final long iid = evt.getItemId();
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
            Long2ObjectMap<List<Event>> result = new Long2ObjectOpenHashMap<List<Event>>(table.size());
            for (Long2ObjectMap.Entry<ImmutableList.Builder<Event>> evt: table.long2ObjectEntrySet()) {
                result.put(evt.getLongKey(), evt.getValue().build());
                evt.setValue(null);
            }
            return result;
        }
    }

    private static enum ItemEventTransform implements Function<Map.Entry<Long,List<Event>>, ItemEventCollection<Event>> {
        INSTANCE {
            @Nullable
            @Override
            public ItemEventCollection<Event> apply(@Nullable Map.Entry<Long, List<Event>> input) {
                if (input == null) {
                    return null;
                } else {
                    return History.forItem(input.getKey(), input.getValue());
                }
            }
        }
    }
}

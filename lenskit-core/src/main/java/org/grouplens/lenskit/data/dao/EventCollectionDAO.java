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

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.lenskit.data.events.Event;
import org.grouplens.lenskit.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.WillClose;
import java.util.*;

/**
 * Data source backed by a collection of events.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class EventCollectionDAO implements EventDAO {
    private static final Logger logger = LoggerFactory.getLogger(EventCollectionDAO.class);
    private Collection<? extends Event> events;
    private Set<Class<? extends Event>> types;

    /**
     * Construct a new data source from a collection of events.
     *
     * @param evts The events to use.
     * @deprecated use {@link #create(Collection)} instead
     */
    @Deprecated
    public EventCollectionDAO(Collection<? extends Event> evts) {
        logger.debug("Creating event collection DAO for {} events", evts.size());
        events = evts;

        // Scan for the types in the data source.
        types = TypeUtils.findTypes(evts, Event.class);
    }

    /**
     * Empty event collection DAO.
     * @return An empty DAO.
     */
    public static EventDAO empty() {
        return create(Collections.<Event>emptyList());
    }

    /**
     * Create a new data source from a collection of events.
     *
     * @param evts          The events collection to be used.
     * @return              A EventCollectionDao generated from events collection.
     */
    public static EventDAO create(Collection<? extends Event> evts){
        EventCollectionDAO ecDAO = new EventCollectionDAO(evts);
        return ecDAO;
    }

    /**
     * Create a new data source from a stream of events.
     *
     * @param stream The event stream to be used.
     * @return An EventCollectionDao generated from events read from the stream.
     */
    public static EventDAO fromStream(@WillClose ObjectStream<Event> stream) {
        EventCollectionDAO ecDAO = new EventCollectionDAO(ObjectStreams.makeList(stream));
        return ecDAO;
    }


    /**
     * Create a new data source from an EventDAO.
     *
     * @param eventDAO The EventDAO to be used.
     * @return A EventCollectionDao generated from events read from the DAO's stream.
     */
    public static EventDAO loadAndWrap(EventDAO eventDAO) {
        return fromStream(eventDAO.streamEvents());
    }

    @Override
    public ObjectStream<Event> streamEvents() {
        return ObjectStreams.wrap(events);
    }

    @Override
    public <E extends Event> ObjectStream<E> streamEvents(Class<E> type) {
        return streamEvents(type, SortOrder.ANY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> ObjectStream<E> streamEvents(Class<E> type, SortOrder order) {
        // We need to filter if there are any event types that are not subtypes of `type`.
        boolean needFilter = Iterables.any(types, Predicates.not(TypeUtils.subtypePredicate(type)));

        Comparator<Event> comp = order.getEventComparator();

        if (!needFilter) {
            // no need to filter - just wrap up our events and cast.
            if (comp == null) {
                return (ObjectStream<E>) ObjectStreams.wrap(events);
            } else {
                @SuppressWarnings("rawtypes")
                List evts = Lists.newArrayList(events);
                Collections.sort(evts, comp);
                return ObjectStreams.wrap(evts);
            }
        } else {
            // Now we must filter our events.
            if (comp == null) {
                return ObjectStreams.filter(streamEvents(), type);
            } else {
                List<E> filtered = Lists.newArrayList(Iterables.filter(events, type));
                Collections.sort(filtered, comp);
                return ObjectStreams.wrap(filtered);
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                          .add("size", events.size())
                          .add("types", types)
                          .toString();
    }
}

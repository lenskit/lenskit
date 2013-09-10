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

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.util.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.grouplens.lenskit.collections.CollectionUtils.fast;

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
     */
    public EventCollectionDAO(Collection<? extends Event> evts) {
        logger.debug("Creating event collection DAO for {} events", evts.size());
        events = evts;

        // Scan for the types in the data source.
        types = TypeUtils.findTypes(fast(evts), Event.class);
    }

    // Create an empty EventCollectionDao object.
    private EventCollectionDAO(){
    }

    /**
     * Create a new data source from a cursor of events.
     *
     * @param eventCursor   The event cursor to be used.
     * @return              A EventCollectionDao generated from events read from the cursor.
     */
    public static EventCollectionDAO CreateEventCollectionDAO(Cursor<Event> eventCursor){
        EventCollectionDAO ecDAO = new EventCollectionDAO();
        List<Event> eventList = new ArrayList<Event>();
        while(eventCursor.hasNext()){
            eventList.add(eventCursor.next());
        }
        eventCursor.close();

        logger.debug("Creating event collection DAO for {} events", eventList.size());
        ecDAO.setEvents(eventList);
        ecDAO.setTypes(TypeUtils.findTypes(fast(ecDAO.getEvents()), Event.class));
        return ecDAO;
    }


    /**
     * Create a new data source from EventDAO.
     *
     * @param eventDAO      The EventDAO to be used.
     * @return              A EventCollectionDao generated from events read from the cursor.
     */
    public static EventCollectionDAO CreateEventCollectionDAO(EventDAO eventDAO){
        return  CreateEventCollectionDAO(eventDAO.streamEvents());
    }

    @Override
    public Cursor<Event> streamEvents() {
        return Cursors.wrap(events);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return streamEvents(type, SortOrder.ANY);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        boolean needFilter = Iterables.any(types, Predicates.not(TypeUtils.subtypePredicate(type)));

        Comparator<Event> comp = order.getEventComparator();

        if (!needFilter) {
            if (comp == null) {
                return (Cursor<E>) Cursors.wrap(events);
            } else {
                List evts = Lists.newArrayList(events);
                Collections.sort(evts, comp);
                return Cursors.wrap(evts);
            }
        } else {
            if (comp == null) {
                return Cursors.filter(streamEvents(), type);
            } else {
                List<E> filtered = Lists.newArrayList(Iterables.filter(events, type));
                Collections.sort(filtered, comp);
                return Cursors.wrap(filtered);
            }
        }
    }


    public Collection<? extends Event> getEvents() {
        return events;
    }

    public void setEvents(Collection<? extends Event> events) {
        this.events = events;
    }

    public Set<Class<? extends Event>> getTypes() {
        return types;
    }

    public void setTypes(Set<Class<? extends Event>> types) {
        this.types = types;
    }

}

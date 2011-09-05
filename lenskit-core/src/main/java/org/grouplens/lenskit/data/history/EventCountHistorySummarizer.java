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
package org.grouplens.lenskit.data.history;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import javax.annotation.Nonnull;

import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.vector.UserVector;

import com.google.common.collect.Iterables;

/**
 * Summarize a history by counting all events referencing an item.  The history
 * can be filtered by type prior to counting.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class EventCountHistorySummarizer implements HistorySummarizer {
    protected final Class<? extends Event> wantedType;

    /**
     * Create a summarizer that counts all events.
     */
    public EventCountHistorySummarizer() {
        this(Event.class);
    }

    /**
     * Create a summarizer that counts events of a particular type.
     * @param type
     */
    public EventCountHistorySummarizer(@Nonnull Class<? extends Event> type) {
        wantedType = type;
    }

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return wantedType;
    }

    @Override
    public UserVector summarize(UserHistory<? extends Event> history) {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        for (Event e: Iterables.filter(history, wantedType)) {
            final long iid = e.getItemId();
            map.put(iid, map.get(iid) + 1);
        }
        return new UserVector(history.getUserId(), map);
    }

    @Override
    public int hashCode() {
        return wantedType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EventCountHistorySummarizer) {
            EventCountHistorySummarizer ocs = (EventCountHistorySummarizer) o;
            return wantedType.equals(ocs.wantedType);
        } else {
            return false;
        }
    }
}

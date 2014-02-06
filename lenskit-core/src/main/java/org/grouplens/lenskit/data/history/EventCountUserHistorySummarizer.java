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
package org.grouplens.lenskit.data.history;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.EventType;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Summarize a history by counting all events referencing an item.  The history
 * can be filtered by type prior to counting.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@Singleton
public final class EventCountUserHistorySummarizer implements UserHistorySummarizer {
    protected final Class<? extends Event> wantedType;

    /**
     * Create a summarizer that counts all events.
     */
    @SuppressWarnings("unused")
    public EventCountUserHistorySummarizer() {
        this(Event.class);
    }

    /**
     * Create a summarizer that counts events of a particular type.
     *
     * @param type The type of event to count.
     */
    @Inject
    public EventCountUserHistorySummarizer(@EventType @Nonnull Class<? extends Event> type) {
        wantedType = type;
    }

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return wantedType;
    }

    @Override @Nonnull
    public SparseVector summarize(@Nonnull UserHistory<? extends Event> history) {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        for (Event e : CollectionUtils.fast(history.filter(wantedType))) {
            final long iid = e.getItemId();
            map.put(iid, map.get(iid) + 1);
        }
        return ImmutableSparseVector.create(map);
    }

    @Override
    public int hashCode() {
        return wantedType.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EventCountUserHistorySummarizer) {
            EventCountUserHistorySummarizer ocs = (EventCountUserHistorySummarizer) o;
            return wantedType.equals(ocs.wantedType);
        } else {
            return false;
        }
    }
}

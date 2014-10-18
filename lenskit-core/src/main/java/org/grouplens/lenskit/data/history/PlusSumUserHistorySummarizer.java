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
package org.grouplens.lenskit.data.history;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.EventType;
import org.grouplens.lenskit.data.event.Plus;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Summarize a history by summing the {@link Plus} values reference an item.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public final class PlusSumUserHistorySummarizer implements UserHistorySummarizer {
    /**
     * Create a new plus sum summarizer.
     */
    @Inject
    public PlusSumUserHistorySummarizer() {
    }

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return Plus.class;
    }

    @Override @Nonnull
    public SparseVector summarize(@Nonnull UserHistory<? extends Event> history) {
        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        for (Event e : CollectionUtils.fast(history)) {
            if (e instanceof Plus) {
                Plus pe = (Plus) e;
                final long iid = pe.getItemId();
                map.put(iid, map.get(iid) + pe.getCount());
            }
        }
        return ImmutableSparseVector.create(map);
    }

    @Override
    public int hashCode() {
        return PlusSumUserHistorySummarizer.class.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PlusSumUserHistorySummarizer;
    }
}

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

import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Like;
import org.grouplens.lenskit.data.event.LikeBatch;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Summarize a history by counting likes (both batched and unbatched).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public final class LikeCountUserHistorySummarizer implements UserHistorySummarizer {
    /**
     * Create a new like count summarizer.  It counts like events and adds {@link LikeBatch} events.
     */
    @Inject
    public LikeCountUserHistorySummarizer() {
    }

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return Event.class;
    }

    @Override @Nonnull
    public SparseVector summarize(@Nonnull UserHistory<? extends Event> history) {
        MutableSparseVector v = MutableSparseVector.create(history.itemSet());
        v.fill(0);
        for (Event e : history) {
            final long iid = e.getItemId();
            if (e instanceof Like) {
                v.add(iid, 1);
            } else if (e instanceof LikeBatch) {
                LikeBatch lb = (LikeBatch) e;
                v.add(iid, lb.getCount());
            }
        }
        v.unsetLessThan(0.1);
        return v.freeze();
    }

    @Override
    public int hashCode() {
        return LikeCountUserHistorySummarizer.class.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LikeCountUserHistorySummarizer;
    }
}

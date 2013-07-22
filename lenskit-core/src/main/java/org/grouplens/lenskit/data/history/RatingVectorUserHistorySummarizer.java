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

import com.google.common.base.Function;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Singleton;
import java.io.Serializable;

/**
 * Summarize a history by extracting a rating vector.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
@ThreadSafe
@Singleton
public final class RatingVectorUserHistorySummarizer implements UserHistorySummarizer, Serializable {
    private static final RatingVectorUserHistorySummarizer INSTANCE = new RatingVectorUserHistorySummarizer();

    private static final long serialVersionUID = 2L;

    @Override
    public Class<? extends Event> eventTypeWanted() {
        return Rating.class;
    }

    @Override
    public SparseVector summarize(UserHistory<? extends Event> history) {
        return history.memoize(SummaryFunction.INSTANCE);
    }

    public static SparseVector makeRatingVector(UserHistory<? extends Event> history) {
        return INSTANCE.summarize(history);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "RatingVectorUserHistorySummarizer";
    }

    /**
     * All rating vector summarizers are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else {
            return getClass().equals(o.getClass());
        }
    }

    static enum SummaryFunction implements Function<UserHistory<? extends Event>, SparseVector> {
        INSTANCE;

        @Nullable
        @Override
        public SparseVector apply(@Nullable UserHistory<? extends Event> history) {
            if (history == null) {
                throw new NullPointerException("history is null");
            }
            return Ratings.userRatingVector(history.filter(Rating.class));
        }
    }
}

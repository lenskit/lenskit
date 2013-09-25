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

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;

/**
 * Summarize user histories as real-valued vectors.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultImplementation(RatingVectorUserHistorySummarizer.class)
public interface UserHistorySummarizer {
    /**
     * Get the supertype of all events required by this summarizer.
     *
     * @return A type such that any type used by this summarizer is a subtype of
     *         it.
     */
    Class<? extends Event> eventTypeWanted();

    /**
     * Compute a vector summary of a user's history.
     *
     *
     * @param history The history to summarize.
     * @return A vector summarizing the user's history.
     */
    @Nonnull SparseVector summarize(@Nonnull UserHistory<? extends Event> history);
}

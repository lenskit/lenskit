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
package org.lenskit.api;

import javax.annotation.Nullable;

/**
 * Main entry point for accessing recommender components.  A recommender object
 * is effectively a recommender <i>session</i>: it is a per-thread or per-request
 * object, likely connected to a database connection or persistence session, that
 * needs to be closed when the client code is finished with it.
 *
 * The various methods in this class return {@code null} if the corresponding
 * operation is not supported by the underlying recommender configuration.  This
 * ensures that, if you can actually get an object implementing a particular interface,
 * you are guaranteed to be able to use it.
 *
 * @compat Public
 * @see RecommenderEngine
 */
public interface Recommender extends AutoCloseable {
    /**
     * Get the recommender's rating scorer.
     *
     * @return The rating predictor for this recommender configuration, or
     *         {@code null} if rating prediction is not supported.
     */
    @Nullable
    RatingPredictor getRatingPredictor();

    /**
     * Get the recommender's item recommender.
     *
     * @return The item recommender for this recommender configuration, or
     *         {@code null} if item recommendation is not supported.
     */
    @Nullable
    ItemRecommender getItemRecommender();

    /**
     * Get the recommender's item scorer.
     * @return The item scorer for the configured recommender, or {@code null} if item scoring is not supported.
     */
    @Nullable
    ItemScorer getItemScorer();

    /**
     * Close the recommender.  This closes underlying resources such as database collections. Components retrieved
     * from the recommender must be used once the recommender is closed.  Components capable of explicitly detecting
     * use-after-close will indicate such invalid use by throwing {@link IllegalStateException}, although they may
     * fail with other exceptions.  The results of using a component after its recommender has been closed are formally
     * undefined.
     */
    @Override
    void close();
}

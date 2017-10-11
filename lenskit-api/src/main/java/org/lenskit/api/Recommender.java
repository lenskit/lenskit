/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
     * Get the recommender's item-based item scorer.
     * @return The item-based item scorer for the configured recommender, or `null` if basket-based scoring is
     * not supported.
     */
    @Nullable
    ItemBasedItemScorer getItemBasedItemScorer();

    /**
     * Get the recommender's item-based item recommender.
     * @return The item-based item recommender for the configured recommender, or `null` if basket-based scoring is
     * not supported.
     */
    @Nullable
    ItemBasedItemRecommender getItemBasedItemRecommender();

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

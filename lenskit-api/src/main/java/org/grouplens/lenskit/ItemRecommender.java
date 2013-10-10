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
package org.grouplens.lenskit;

import org.grouplens.lenskit.scored.ScoredId;

import it.unimi.dsi.fastutil.longs.LongSet;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Interface for recommending items. Several methods are provided, of varying
 * generality.
 *
 * <p>
 * The core idea of the recommend API is to recommend <i>n</i> items for a user,
 * where the items recommended are taken from a set of candidate items and
 * further constrained by an exclude set of forbidden items. Items in the
 * candidate set but not in the exclude set are considered viable for
 * recommendation.
 * </p>
 *
 * <h2>Candidate Items</h2>
 * <p>
 * By default, the candidate set is the universe of all items the recommender
 * knows about. The default exclude set is somewhat more subtle. Its exact
 * definition varies across implementations, but will be the set of items the
 * system believes the user will not be interested in by virtue of already
 * having or knowing about them. For example, rating-based recommenders will
 * exclude the items the user has rated, and purchase-based recommenders will
 * typically exclude items the user has purchased. Some implementations may
 * allow this to be configured. Client code always has the option of manually
 * specifying the exclude set, however, so applications with particular needs in
 * this respect can manually provide the sets they need respected.
 * </p>
 *
 * <h2>Ordering</h2>
 * <p>
 * If the recommender has an opinion about the order in which recommendations should be displayed,
 * it will return the items in that order.  For many recommenders, this will be descending order
 * by score; however, this interface imposes no such limitation.
 * </p>
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public interface ItemRecommender {
    /**
     * Recommend all possible items for a user using the default exclude set.
     *
     * @param user The user ID.
     * @return The recommended items.
     * @see #recommend(long, int, Set, Set)
     */
    List<ScoredId> recommend(long user);

    /**
     * Recommend up to {@var n} items for a user using the default exclude
     * set.
     *
     * @param user The user ID.
     * @param n    The number of recommendations to return.
     * @return The recommended items.
     * @see #recommend(long, int, Set, Set)
     */
    List<ScoredId> recommend(long user, int n);

    /**
     * Recommend all possible items for a user from a set of candidates using
     * the default exclude set.
     *
     * @param user       The user ID.
     * @param candidates The candidate set (can be null to represent the
     *                   universe).
     * @return The recommended items.
     * @see #recommend(long, int, Set, Set)
     */
    List<ScoredId> recommend(long user, @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set and an exclude set. The exclude set is applied to
     * the candidate set, so the final effective candidate set is
     * {@var canditates} minus {@var exclude}.
     *
     * @param user       The user's ID
     * @param n          The number of ratings to return. If negative, there is
     *                   no specific recommendation list size requested.
     * @param candidates A set of candidate items which can be recommended. If
     *                   {@code null}, all items are considered candidates.
     * @param exclude    A set of items to be excluded. If {@code null}, a default
     *                   exclude set is used.
     * @return A list of recommended items. If the recommender cannot assign
     *         meaningful scores, the scores will be {@link Double#NaN}. For
     *         most scoring recommenders, the items will be ordered in
     *         decreasing order of score. This is not a hard requirement — e.g.
     *         set recommenders are allowed to be more flexible.
     */
    List<ScoredId> recommend(long user, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude);
    
    /**
     * Determine the items for which predictions can be made for a certain user.
     * This allows us to ask for predictions when no candidates are available.
     *
     * @param user The user's ID.
     * @return All items for which predictions can be generated for the user.
     */
    LongSet getPredictableItems(long user);
    
    /**
     * Get the default exclude set for a user.   
     * This allows us to ask for predictions when no candidates are available.
     *
     * @param user The user ID.
     * @return The set of items to exclude.
     */
    LongSet getDefaultExcludes(long user);

}

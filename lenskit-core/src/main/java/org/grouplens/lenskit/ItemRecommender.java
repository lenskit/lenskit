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
package org.grouplens.lenskit;

import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.lenskit.data.ScoredLongList;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.history.UserHistory;

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
 *
 * <p>
 * As with {@link ItemScorer}, this interface supports both ID-based and
 * history-based recommendation. The {@link #canUseHistory()} method allows this
 * to be queried.
 *
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
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface ItemRecommender {
    /**
     * Recommend all possible items for a user using the default exclude set.
     *
     * @param user The user ID.
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user);

    /**
     * Recommend up to <var>n</var> items for a user using the default exclude
     * set.
     *
     * @param user The user ID.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user, int n);

    /**
     * Recommend all possible items for a user from a set of candidates using
     * the default exclude set.
     *
     * @param user The user ID.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #recommend(long, int, Set, Set)
     */
    ScoredLongList recommend(long user, @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set and an exclude set. The exclude set is applied to
     * the candidate set, so the final effective candidate set is
     * <var>canditates</var> minus <var>exclude</var>.
     *
     * @param user The user's ID
     * @param n The number of ratings to return. If negative, recommend all
     *        possible items.
     * @param candidates A set of candidate items which can be recommended. If
     *        <tt>null</tt>, all items are considered candidates.
     * @param exclude A set of items to be excluded. If <tt>null</tt>, a default
     *        exclude set is used.
     * @return A list of recommended items. If the recommender cannot assign
     *         meaningful scores, the scores will be {@link Double#NaN}. For
     *         most scoring recommenders, the items should be ordered in
     *         decreasing order of score. This is not a hard requirement — e.g.
     *         set recommenders are allowed to be more flexible.
     */
    ScoredLongList recommend(long user, int n, @Nullable Set<Long> candidates,
                             @Nullable Set<Long> exclude);

    /**
     * Query whether this recommender can take advantage of user history.
     *
     * @return <tt>true</tt> if the history-based methods can use the history,
     *         or <tt>false</tt> if they will ignore it in favor of model-based
     *         data.
     */
    boolean canUseHistory();

    /**
     * Recommend all possible items for a user with the default exclude set.
     *
     * @param profile The user profile.
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile);

    /**
     * Recommend up to <var>n</var> items for a user using the default exclude
     * set.
     *
     * @param profile The user profile.
     * @param n The number of recommendations to return.
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile, int n);

    /**
     * Recommend all possible items for a user from a set of candidates using
     * the default exclude set.
     *
     * @param profile The user profile.
     * @param candidates The candidate set (can be null to represent the
     *        universe).
     * @return The sorted list of scored items.
     * @see #recommend(UserHistory, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile,
            @Nullable Set<Long> candidates);

    /**
     * Produce a set of recommendations for the user.
     *
     * @param profile The user profile.
     * @param n The number of ratings to return. If negative, recommend all
     *        possible items.
     * @param candidates A set of candidate items which can be recommended. If
     *        <tt>null</tt>, the candidate set is considered to contain the
     *        universe.
     * @param exclude A set of items to be excluded. If <tt>null</tt>, the
     *        default exclude set is used. Exclusions are applied to the
     *        candidate set, so the final candidate set is <var>candidates</var>
     *        minus <var>exclude</var>.
     * @return a list of scored recommendations, sorted in nondecreasing order
     *         of score.
     * @see ItemRecommender#recommend(long, int, Set, Set)
     */
    public ScoredLongList recommend(UserHistory<? extends Event> profile, int n,
            @Nullable Set<Long> candidates, @Nullable Set<Long> exclude);
}

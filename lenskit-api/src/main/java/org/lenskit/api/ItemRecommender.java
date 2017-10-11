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
import java.util.List;
import java.util.Set;

/**
 * Interface for recommending items. This interface provides APIs for both basic (recommend for a given user)
 * and complex (recommend at most *n* items from a given set to the user) recommendation tasks.
 *
 * The core idea of the recommend API is to recommend *n* items for a user,
 * where the items recommended are taken from a set of candidate items and
 * further constrained by an exclude set of forbidden items.
 *
 * ## Candidate Items
 *
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
 * ## Ordering
 *
 * If the recommender has an opinion about the order in which recommendations should be displayed,
 * the result set will present items in that order.  For many recommenders, this will be descending order
 * by score; however, this interface does not guarantee a relationship between scores and ordering.
 *
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
    List<Long> recommend(long user);

    /**
     * Recommend up to `n` items for a user using the default exclude
     * set.
     *
     * @param user The user ID.
     * @param n    The number of recommendations to return. Negative values request as many recommendations
     *             as possible.
     * @return The recommended items.
     * @see #recommend(long, int, Set, Set)
     */
    List<Long> recommend(long user, int n);

    /**
     * Produce a set of recommendations for the user. This is the most general
     * recommendation method, allowing the recommendations to be constrained by
     * both a candidate set \\(\\mathcal{C}\\) and an exclude set \\(\\mathcal{E}\\). The exclude set is applied to
     * the candidate set, so the final effective candidate set is \\(\\mathcal{C} \\backslash \\mathcal{E}\\).
     *
     * The recommender is *not* guaranteed to return a full `n` recommendations.  There are many reasons
     * why it might return a shorter list, including lack of items, lack of coverage for items, or a
     * predefined notion of a maximum recommendation list length.  However, a negative value for `n` instructs
     * the recommender to return as many as it can consistent with any limitations built in to its design and/or
     * supporting algorithms.
     *
     * @param user       The user's ID
     * @param n          The number of ratings to return. If negative, the recommender
     *                   will return as many recommendations as possible.
     * @param candidates A set of candidate items which can be recommended. If
     *                   {@code null}, all items are considered candidates.
     * @param exclude    A set of items to be excluded. If {@code null}, a default
     *                   exclude set is used.
     * @return A list of recommended items.
     */
    List<Long> recommend(long user, int n, @Nullable Set<Long> candidates,
                         @Nullable Set<Long> exclude);

    /**
     * Produce a set of recommendations for the user with additional details. This method functions identically to
     * {@link #recommend(long, int, Set, Set)}, except that it may produce more detailed results. Implementations may
     * return subclasses of {@link ResultList} that provide access to additional details about each recommendation.
     *
     * @param user       The user's ID
     * @param n          The number of ratings to return. If negative, then the recommender will return as many
     *                   recommendations as possible.
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
    ResultList recommendWithDetails(long user, int n, @Nullable Set<Long> candidates,
                                    @Nullable Set<Long> exclude);
}

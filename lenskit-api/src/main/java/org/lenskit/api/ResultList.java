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

import java.util.List;

/**
 * A set of results from a recommender operation.  Recommendation results conceptually map item
 * (or user, for a few operations) IDs to corresponding scores, and have some order over their IDs.
 * Depending on the operation that produced the result set, that order may be arbitrary, but
 * operations that find things (recommendation, related item and user queries) will usually
 * return results in order from most to least relevant or wanted.
 *
 * @param <E> The entry type (parameterized to make extension cleaner).
 */
public interface ResultList<E extends Result> extends List<E> {
    /**
     * Return a list of the item (or user) IDs in this result list.
     *
     * @return The list of IDs.
     */
    List<Long> idList();

    /**
     * Convert this result list to a list with a different type of result.  Technically, this provides a runtime-checked
     * means of making the result list type *covariant* in its result type, since Java does not allow us to encode this
     * allowance in the type system.
     *
     * @param type The result type to cast to.  It is always valid for this type to be a supertype of `E`; the method
     *             will also succeed if every non-null result is an instance of this type (or one of its subtypes).
     * @param <T> The result type.
     * @return A result list statically typed to contain results of type `T`.
     * @throws IllegalArgumentException if not all results can be cast type type `T`.
     * @throws UnsupportedOperationException if the result list is mutable (very rare, only used for intermediate
     * working objects).
     */
    <T extends Result> ResultList<T> castResults(Class<T> type);
}

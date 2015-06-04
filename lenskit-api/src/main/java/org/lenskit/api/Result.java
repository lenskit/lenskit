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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A LensKit result, consisting of a score and an ID.  Individual recommenders may subclass this component to provide
 * more detailed results.
 *
 * Implementations must create well-defined equality. However, instances of different implementations are not required
 * or expected to be able to be equal to each other.
 */
public interface Result {
    /**
     * Get the ID for the result.
     * @return The user or item ID for this result.
     */
    long getId();

    /**
     * Get the score (value) associated with this result.
     * @return The score associated with this entry (or {@link Double#NaN} if the result does not have a score).
     */
    double getScore();

    /**
     * Query whether the result has a score.  This is equivalent to testing the return value of {@link #getScore()} with
     * {@link Double#isNaN(double)}, but may make the resulting code more readable.
     *
     * @return `true` if the result has a score.
     */
    boolean hasScore();

    /**
     * Convert this result to the specified type, if possible.
     *
     * @param type The desired result type.
     * @param <T> The desired result type.
     * @return The result as a result of type `T`, or `null` if the type cannot be cast.  This may be done by casting
     * this type, or by unwrapping wrapper result types, but it will not search multiple alternatives.
     * @see #find(Class)
     */
    @Nullable
    <T extends Result> T as(@Nonnull Class<T> type);

    /**
     * Attempt to view this result as another type, searching through alternatives if necessary.
     *
     * @param type The type to find.
     * @param <T> The desired result type.
     * @return The first result of type `T` found by searching this result and all results it contains.  Each type of
     * result will define some search order for the purpose of defining the 'first' result of type `T`.
     * @see #as(Class)
     */
    @Nullable
    <T extends Result> T find(@Nonnull Class<T> type);
}

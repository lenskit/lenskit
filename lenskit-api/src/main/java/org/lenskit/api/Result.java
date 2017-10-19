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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A LensKit result, consisting of a score and an ID.  Individual recommenders may subclass this component to provide
 * more detailed results.
 *
 * Implementations must create well-defined equality. However, instances of different implementations are not required
 * or expected to be able to be equal to each other.  Instances are not allowed to be equal if they have different IDs.
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
     * this type, or by unwrapping wrapper result types.
     */
    @Nullable
    <T extends Result> T as(@Nonnull Class<T> type);
}

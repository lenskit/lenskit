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
package org.lenskit.basic;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Result from a {@link FallbackItemScorer}.
 */
public class FallbackResult implements Result {
    private final Result innerResult;
    private final boolean fromPrimary;

    FallbackResult(Result r, boolean prim) {
        fromPrimary = prim;
        innerResult = r;
    }

    @Override
    public long getId() {
        return innerResult.getId();
    }

    @Override
    public double getScore() {
        return innerResult.getScore();
    }

    @Override
    public boolean hasScore() {
        return innerResult.hasScore();
    }

    /**
     * Convert this result to another type.  If the type is not a superclass of {@code FallbackResult}, then this
     * method delegates to the {@linkplain #getInnerResult() inner result}.
     * @param type The desired result type.
     * @param <T> The target result type.
     * @return The converted type, or {@code null} if the result cannot be converted.
     */
    @Nullable
    @Override
    public <T extends Result> T as(@Nonnull Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        } else {
            return innerResult.as(type);
        }
    }

    /**
     * Query whether this result came from the primary or the baseline item scorer.
     * @return {@code true} if the result came from the primary item scorer.
     */
    public boolean isFromPrimary() {
        return fromPrimary;
    }

    /**
     * Get the result from the scorer that produced this score.
     * @return The inner result that produced the score.
     */
    public Result getInnerResult() {
        return innerResult;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FallbackResult that = (FallbackResult) o;

        return new EqualsBuilder()
                .append(fromPrimary, that.fromPrimary)
                .append(innerResult, that.innerResult)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(innerResult)
                .append(fromPrimary)
                .toHashCode();
    }
}

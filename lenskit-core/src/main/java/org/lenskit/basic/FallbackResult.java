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

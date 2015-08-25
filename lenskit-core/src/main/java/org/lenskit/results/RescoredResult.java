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
package org.lenskit.results;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.api.Result;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A result that wraps another result with a different score.
 *
 * @see Results#rescore(Result, double)
 * @see Results#rescore(Result, Result)
 */
public final class RescoredResult implements Result {
    private final Result original;
    private final Result result;

    RescoredResult(@Nonnull Result orig, @Nullable Result res) {
        original = orig;
        result = res;
    }

    /**
     * Get the original result (before rescoring).
     * @return The original result.
     */
    public Result getOriginalResult() {
        return original;
    }

    /**
     * Get the final result (after rescoring). If additional details are available on the new score, this result will
     * carry them.  If no additional details are available, it may just be this result.
     * @return The final result with any available details.
     */
    public Result getFinalResult() {
        return result;
    }

    @Override
    public long getId() {
        return original.getId();
    }

    @Override
    public double getScore() {
        return result != null ? result.getScore() : Double.NaN;
    }

    @Override
    public boolean hasScore() {
        return result != null && result.hasScore();
    }

    /**
     * Convert this type.  It searches the types in the following order:
     *
     * 1. This class.
     * 2. The final result.
     * 3. The original result.
     *
     * @param type The desired result type.
     * @param <T> The desired result type.
     * @return The result, if available.
     */
    @Override
    public <T extends Result> T as(@Nonnull Class<T> type) {
        T converted;
        if (type.isInstance(this)) {
            return type.cast(this);
        } else if ((converted = result.as(type)) != null) {
            return converted;
        } else {
            return original.as(type);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(original)
                                    .append(result)
                                    .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RescoredResult) {
            RescoredResult cr = (RescoredResult) obj;
            return original.equals(cr.getOriginalResult()) && result.equals(cr.getFinalResult());
        } else {
            return false;
        }
    }
}

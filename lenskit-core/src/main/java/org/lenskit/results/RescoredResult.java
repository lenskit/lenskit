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

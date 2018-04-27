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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.api.Result;
import org.lenskit.util.keys.KeyedObject;

import javax.annotation.Nonnull;

/**
 * Base class for basic result types.  It provides storage for the ID and score, as well as helper methods for hashing
 * and equality checking.  This type does not directly enforce immutability, but subclasses should be immutable.
 */
public abstract class AbstractResult implements Result, KeyedObject {
    protected long id;
    protected double score;

    /**
     * Create a new result.
     * @param id The result ID.
     * @param score The result score.
     */
    protected AbstractResult(long id, double score) {
        this.id = id;
        this.score = score;
    }

    /**
     * Create a new, uninitialized result.
     */
    protected AbstractResult() {}

    @Override
    public long getKey() {
        return getId();
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public double getScore() {
        return score;
    }

    @Override
    public boolean hasScore() {
        return !Double.isNaN(score);
    }

    /**
     * {@inheritDoc}
     *
     * The default implementation simply casts the result to type `type` if possible.
     */
    @Override
    public <T extends Result> T as(@Nonnull Class<T> type) {
        if (type.isInstance(this)) {
            return type.cast(this);
        } else {
            return null;
        }
    }

    /**
     * Create a hash code builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * building a hash code.
     *
     * @return A hash code builder that has the ID and score already appended.
     */
    protected HashCodeBuilder startHashCode() {
        return startHashCode(this);
    }

    /**
     * Create an equality builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * checking equality.
     *
     * @param r The other result.
     * @return An equality builder, that has the ID and score of this result and `r` already appended to it.
     */
    protected EqualsBuilder startEquality(Result r) {
        return startEquality(this, r);
    }

    /**
     * Create an equality builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * checking equality.
     *
     * @param r1 The first result.
     * @param r2 The other result.
     * @return An equality builder, that has the ID and score of this result and `r` already appended to it.
     */
    public static EqualsBuilder startEquality(Result r1, Result r2) {
        return new EqualsBuilder().append(r1.getId(), r2.getId())
                                  .append(r1.getScore(), r2.getScore());
    }

    /**
     * Create an equality builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * checking equality.
     *
     * @param r The other result.
     * @return An equality builder, that has the ID and score of this result and `r` already appended to it.
     */
    public static HashCodeBuilder startHashCode(Result r) {
        return new HashCodeBuilder().append(r.getId()).append(r.getScore());
    }
}

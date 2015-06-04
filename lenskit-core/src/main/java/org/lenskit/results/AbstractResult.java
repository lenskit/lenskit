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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.api.Result;

/**
 * Base class for basic result types.  It provides storage for the ID and score, as well as helper methods for hashing
 * and equality checking.  This type does not directly enforce immutability, but subclasses should be immutable.
 */
public class AbstractResult implements Result {
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
     * Create a hash code builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * building a hash code.
     *
     * @return A hash code builder that has the ID and score already appended.
     */
    protected HashCodeBuilder startHashCode() {
        return new HashCodeBuilder().append(id).append(score);
    }

    /**
     * Create an equality builder, populated with the ID and score.  Subclasses can use this as a starting point for
     * checking equality.
     *
     * @param r The other result.
     * @return An equality builder, that has the ID and score of this result and `r` already appended to it.
     */
    protected EqualsBuilder startEquality(Result r) {
        return new EqualsBuilder().append(id, r.getId())
                                  .append(score, r.getScore());
    }
}

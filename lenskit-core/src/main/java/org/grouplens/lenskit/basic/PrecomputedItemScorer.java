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
package org.grouplens.lenskit.basic;

import it.unimi.dsi.fastutil.longs.*;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * An item scorer that stores a precomputed map of item scores.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class PrecomputedItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2ObjectMap<ImmutableSparseVector> userData;

    private PrecomputedItemScorer(Long2ObjectMap<? extends SparseVector> udat) {
        userData = new Long2ObjectOpenHashMap<ImmutableSparseVector>(udat.size());
        for (Long2ObjectMap.Entry<? extends SparseVector> e: udat.long2ObjectEntrySet()) {
            userData.put(e.getLongKey(), e.getValue().immutable());
        }
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        SparseVector sv = userData.get(user);
        scores.clear();
        if (sv != null) {
            scores.set(sv);
        }
    }

    /**
     * Builder for mock item scorers.
     */
    public static class Builder {
        private final Long2ObjectMap<Long2DoubleMap> userData;

        public Builder() {
            userData = new Long2ObjectOpenHashMap<Long2DoubleMap>();
        }

        /**
         * Add an user's entire score vector to the mock scorer.
         * @param user The user.
         * @param usv The vector of scores to use. This will replace any previously-added scores.
         * @return The builder (for chaining).
         */
        public Builder addUser(long user, SparseVector usv) {
            Long2DoubleMap msv = userData.get(user);
            if (msv == null) {
                msv = new Long2DoubleOpenHashMap();
                userData.put(user, msv);
            }
            for (VectorEntry e: usv) {
                msv.put(e.getKey(), e.getValue());
            }
            return this;
        }

        /**
         * Add a score. When the item is scored for the user, the mock scorer will return this score.
         *
         * @param user The user ID.
         * @param item The item ID.
         * @param score The score to return.
         * @return The builder (for chaining).
         */
        public Builder addScore(long user, long item, double score) {
            Long2DoubleMap msv = userData.get(user);
            if (msv == null) {
                msv = new Long2DoubleOpenHashMap();
                userData.put(user, msv);
            }
            msv.put(item, score);
            return this;
        }

        /**
         * Construct the mock item scorer.  This will empty the builder.
         * @return A mock item scorer that will return the configured scores.
         */
        public PrecomputedItemScorer build() {
            Long2ObjectMap<ImmutableSparseVector> vectors =
                    new Long2ObjectOpenHashMap<ImmutableSparseVector>(userData.size());
            for (Long2ObjectMap.Entry<Long2DoubleMap> entry: userData.long2ObjectEntrySet()) {
                vectors.put(entry.getLongKey(), ImmutableSparseVector.create(entry.getValue()));
            }
            userData.clear();
            return new PrecomputedItemScorer(vectors);
        }
    }

    /**
     * Construct a new builder for precomputed item scorers.  This is useful for building item
     * scorers for mocks.
     *
     * @return A new builder for item scorers.
     */
    public static Builder newBuilder() {
        return new Builder();
    }
}

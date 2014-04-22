/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

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
        private final Long2ObjectOpenHashMap<MutableSparseVector> userData;

        public Builder() {
            userData = new Long2ObjectOpenHashMap<MutableSparseVector>();
        }

        /**
         * Add an user's entire score vector to the mock scorer.
         * @param user The user.
         * @param usv The vector of scores to use. This will replace any previously-added scores.
         * @return The builder (for chaining).
         */
        public Builder addUser(long user, SparseVector usv) {
            userData.put(user, usv.mutableCopy());
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
            MutableSparseVector msv = userData.get(user);
            if (msv == null || !msv.keyDomain().contains(item)) {
                LongSet domain = new LongOpenHashSet();
                if (msv == null) {
                    domain.add(item);
                    msv = MutableSparseVector.create(domain);
                } else {
                    domain.addAll(msv.keyDomain());
                    domain.add(item);
                    SparseVector save = msv;
                    msv = MutableSparseVector.create(domain);
                    msv.set(save);
                }
                userData.put(user, msv);
            }
            msv.set(item, score);
            return this;
        }

        /**
         * Construct the mock item scorer.
         * @return A mock item scorer that will return the configured scores.
         */
        public PrecomputedItemScorer build() {
            return new PrecomputedItemScorer(userData);
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

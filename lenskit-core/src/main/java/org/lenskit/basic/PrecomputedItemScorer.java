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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * An item scorer that stores a precomputed map of item scores.
 *
 * @since 3.0
 */
public class PrecomputedItemScorer extends AbstractItemScorer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Long2ObjectOpenHashMap<KeyedObjectMap<Result>> userData;

    private PrecomputedItemScorer(Long2ObjectMap<KeyedObjectMap<Result>> udat) {
        userData = new Long2ObjectOpenHashMap<>(udat);
    }

    @Override
    public Result score(long user, long item) {
        KeyedObjectMap<Result> ur = userData.get(user);
        return ur != null ? ur.get(item) : null;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        List<Result> rs = new ArrayList<>(items.size());
        KeyedObjectMap<Result> userResults = userData.get(user);
        if (userResults != null) {
            LongIterator iter = LongIterators.asLongIterator(items.iterator());
            while (iter.hasNext()) {
                long item = iter.nextLong();
                Result r = userResults.get(item);
                if (r != null) {
                    rs.add(r);
                }
            }
        }
        return Results.newResultMap(rs);
    }

    /**
     * Builder for mock item scorers.
     */
    public static class Builder {
        private final Long2ObjectMap<List<Result>> userData;

        public Builder() {
            userData = new Long2ObjectOpenHashMap<>();
        }

        /**
         * Add results for a user.
         * @param user The user.
         * @param scores The scores to add.
         * @return The builder (for chaining).
         */
        public Builder addResults(long user, Result... scores) {
            List<Result> msv = userData.get(user);
            if (msv == null) {
                msv = new ArrayList<>();
                userData.put(user, msv);
            }
            msv.addAll(Arrays.asList(scores));
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
            return addResults(user, Results.create(item, score));
        }

        /**
         * Construct the mock item scorer.  This will empty the builder.
         * @return A mock item scorer that will return the configured scores.
         */
        public PrecomputedItemScorer build() {
            Long2ObjectMap<KeyedObjectMap<Result>> vectors =
                    new Long2ObjectOpenHashMap<>(userData.size());
            for (Long2ObjectMap.Entry<List<Result>> entry: userData.long2ObjectEntrySet()) {
                vectors.put(entry.getLongKey(), KeyedObjectMap.create(entry.getValue(), Results.keyExtractor()));
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

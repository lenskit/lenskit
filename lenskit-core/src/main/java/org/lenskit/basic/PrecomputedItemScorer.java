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

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.text.StringTokenizer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.inject.Shareable;
import org.lenskit.results.Results;
import org.lenskit.util.io.LineStream;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.KeyedObjectMap;

import javax.annotation.Nonnull;
import javax.annotation.WillClose;
import java.io.BufferedReader;
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
@Shareable
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
     * Read predictions from a CSV file.
     * @param buf A CSV file reader.
     * @return The item scorer.
     */
    public static PrecomputedItemScorer fromCSV(@WillClose BufferedReader buf) {
        StringTokenizer tok = new StringTokenizer((String) null, ",");
        Builder bld = new Builder();
        try (ObjectStream<List<String>> rows = new LineStream(buf).tokenize(tok)) {
            for (List<String> row : rows) {
                // FIXME Add error checking
                long user = Long.parseLong(row.get(0));
                long item = Long.parseLong(row.get(1));
                double score = Double.parseDouble(row.get(2));
                bld.addScore(user, item, score);
            }
        }
        return bld.build();
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

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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongList;
import org.grouplens.lenskit.baseline.ScoreSource;
import org.grouplens.lenskit.symbols.TypedSymbol;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.results.BasicResultMap;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Item scorer that combines a primary scorer with a baseline.  This scorer is comprised of two
 * other scorers, a primary scorer and a baseline scorer.  It first scores items using the primary
 * scorer, and then consults the baseline scorer for any items that the primary scorer could not
 * score.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class FallbackItemScorer extends AbstractItemScorer {
    public static final TypedSymbol<ScoreSource> SCORE_SOURCE_SYMBOL =
            TypedSymbol.of(ScoreSource.class, "org.grouplens.lenskit.baseline.ScoreSource");
    private final ItemScorer primaryScorer;
    private final ItemScorer baselineScorer;

    @Inject
    public FallbackItemScorer(@PrimaryScorer ItemScorer primary,
                              @BaselineScorer ItemScorer baseline) {
        primaryScorer = primary;
        baselineScorer = baseline;
    }

    @Override
    public FallbackResult score(long user, long item) {
        Result r = primaryScorer.score(user, item);
        if (r != null) {
            return new FallbackResult(r, true);
        }

        r = baselineScorer.score(user, item);
        if (r != null) {
            return new FallbackResult(r, false);
        } else {
            return null;
        }
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        ResultMap results = primaryScorer.scoreWithDetails(user, items);
        List<Result> allResults = new ArrayList<>(items.size());

        LongList toFetch = new LongArrayList(items.size() - results.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            final long item = iter.nextLong();
            Result r = results.get(item);
            if (r == null) {
                toFetch.add(item);
            } else {
                allResults.add(new FallbackResult(r, true));
            }
        }

        if (!toFetch.isEmpty()) {
            for (Result r: baselineScorer.scoreWithDetails(user, toFetch)) {
                allResults.add(new FallbackResult(r, false));
            }
        }

        return new BasicResultMap(allResults);
    }

    /**
     * Get the primary scorer from this item scorer.
     * @return The scorer's primary scorer.
     * @see PrimaryScorer
     */
    @Nonnull
    public ItemScorer getPrimaryScorer() {
        return primaryScorer;
    }

    /**
     * Get the baseline scorer from this item scorer.
     * @return The scorer's baseline scorer.
     * @see BaselineScorer
     */
    @Nonnull
    public ItemScorer getBaselineScorer() {
        return baselineScorer;
    }

}

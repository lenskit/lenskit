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

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongList;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.results.BasicResultMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
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

    /**
     * An item scorer provider for opportunistically creating fallback scorers.  If a baseline scorer is configured,
     * this provider returns a fallback scorer that uses it; otherwise, it just returns the primary scorer.
     */
    public static class DynamicProvider implements Provider<ItemScorer> {
        private final ItemScorer primary;
        private final ItemScorer fallback;

        @Inject
        public DynamicProvider(@PrimaryScorer ItemScorer prim,
                               @Nullable @BaselineScorer ItemScorer fb) {
            primary = prim;
            fallback = fb;
        }

        @Override
        public ItemScorer get() {
            if (fallback == null) {
                return primary;
            } else {
                return new FallbackItemScorer(primary, fallback);
            }
        }
    }
}

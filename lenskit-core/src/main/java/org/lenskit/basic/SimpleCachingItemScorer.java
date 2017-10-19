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

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.ResultMap;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

/**
 * A simple cached item scorer that remembers the result for the last user id it scored.
 *
 *  @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class SimpleCachingItemScorer extends AbstractItemScorer {
    private long cachedId = -1;
    private ResultMap cachedScores = null;
    private final ItemScorer scorer;

    @Inject
    public SimpleCachingItemScorer(ItemScorer sc) {
        scorer = sc;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        if(cachedId == user && cachedScores != null) {
            LongSet cachedItems = LongUtils.asLongSet(cachedScores.keySet());
            if (!cachedItems.containsAll(LongUtils.asLongCollection(items))) {
                LongSet reqItems = LongUtils.packedSet(items);
                LongSortedSet diffItems = LongUtils.setDifference(reqItems, cachedItems);
                ResultMap newCache = scorer.scoreWithDetails(user, diffItems);
                cachedScores = Results.newResultMap(Iterables.concat(cachedScores, newCache));
            }
        } else {
            cachedScores = scorer.scoreWithDetails(user, items);
            cachedId = user;
        }
        return cachedScores;
    }

    public long getId() {
        return cachedId;
    }

    public ResultMap getCache() {
        return cachedScores;
    }

}

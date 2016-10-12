/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
            Set<Long> cachedItems = cachedScores.keySet();
            if(!cachedItems.containsAll(items)) {
                LongSet reqItems = LongUtils.packedSet(items);
                LongSortedSet diffItems = LongUtils.setDifference(reqItems, LongUtils.asLongSet(cachedItems));
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

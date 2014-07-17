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

import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.collections.LongUtils;
import org.grouplens.lenskit.vectors.ImmutableSparseVector;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * A simple cached item scorer that remembers the result for the last user id it scored.
 *
 *  @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class SimpleCachingItemScorer extends AbstractItemScorer{
    private long cachedId = -1;
    private ImmutableSparseVector cachedScores = null;
    private final ItemScorer scorer;

    @Inject
    public SimpleCachingItemScorer(ItemScorer sc) {
        scorer = sc;
    }

    /**
     * For each input, check with the cached user id. If the requested items
     * is a subset of the cached items, use the cache; otherwise score the
     * new items and update the cached scores.
     */
    @Override
    public void score(long user, @Nonnull MutableSparseVector scores){
        LongSortedSet reqItems = scores.keyDomain();
        if(cachedId == user && cachedScores != null) {
            LongSortedSet cachedItems = cachedScores.keyDomain();
            if(!cachedItems.containsAll(reqItems)) {
                LongSortedSet diffItems = LongUtils.setDifference(reqItems, cachedItems);
                SparseVector newCache = scorer.score(user, diffItems);
                cachedScores = cachedScores.combineWith(newCache);
            }
            scores.set(cachedScores);
        } else {
            scorer.score(user, scores);
            cachedScores = scores.immutable();
            cachedId = user;
        }
    }

    public long getId() {
        return cachedId;
    }

    public SparseVector getCache() {
        return cachedScores;
    }

}

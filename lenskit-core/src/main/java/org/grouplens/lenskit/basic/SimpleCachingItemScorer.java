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

import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Collection;

/**
 * A simple cached item scorer that remembers the result for the last user id it scored.
 *
 *  @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */

public class SimpleCachingItemScorer extends AbstractItemScorer{
    protected long cachedId = -1;
    protected SparseVector cachedScores = null;
    protected ItemScorer scorer;

    @Inject
    public SimpleCachingItemScorer(@Nonnull ItemScorer sc) {
        scorer = sc;
    }

    /**
     * For each input, check with the cached user id. If the id is the same, directly return the cached
     * result. Otherwise, call the scorer.
     * {@inheritDoc}
     * <p>Delegates to {@link #score(long, org.grouplens.lenskit.vectors.MutableSparseVector)}.
     */
    @Nonnull
    @Override
    public SparseVector score(long user, @Nonnull Collection<Long> items) {
        if(user == cachedId & cachedScores != null) {
            return cachedScores;
        } else {
            MutableSparseVector scores = MutableSparseVector.create(items);
            score(user, scores);
            cachedId = user;
            cachedScores = scores.freeze();
            // FIXME Create a more efficient way of "releasing" mutable sparse vectors
            return cachedScores;
        }
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores){
        scorer.score(user, scores);
    }

    public long getId() {
        return cachedId;
    }

    public SparseVector getCache() {
        return cachedScores;
    }

}

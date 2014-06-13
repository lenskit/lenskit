/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdListBuilder;
import org.grouplens.lenskit.scored.ScoredIds;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.Collections;
import java.util.List;

/**
 * Scored item accumulator with no upper bound.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class UnlimitedScoredItemAccumulator implements ScoredItemAccumulator {
    private ScoredIdListBuilder scores;

    public UnlimitedScoredItemAccumulator() {}

    @Override
    public boolean isEmpty() {
        return scores == null || scores.size() == 0;
    }

    @Override
    public int size() {
        return scores == null ? 0 : scores.size();
    }

    @Override
    public void put(long item, double score) {
        if (scores == null) {
            scores = ScoredIds.newListBuilder();
        }
        scores.add(item, score);
    }

    @Override
    public List<ScoredId> finish() {
        if (scores == null) {
            return Collections.emptyList();
        }
        List<ScoredId> list = scores.sort(ScoredIds.scoreOrder().reverse()).finish();
        scores = null;
        return list;
    }

    @Override
    public MutableSparseVector finishVector() {
        if (scores == null) {
            return MutableSparseVector.create();
        }

        MutableSparseVector vec = scores.buildVector().mutableCopy();
        scores.clear();
        scores = null;
        return vec;
    }

    @Override
    public LongSet finishSet() {
        if (scores == null) {
            return LongSets.EMPTY_SET;
        }

        LongSet set = new LongOpenHashSet(scores.size());
        for (ScoredId id: CollectionUtils.fast(finish())) {
            set.add(id.getId());
        }
        return set;
    }
}

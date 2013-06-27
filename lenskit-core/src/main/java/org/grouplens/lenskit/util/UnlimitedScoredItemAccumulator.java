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
package org.grouplens.lenskit.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;
import org.grouplens.lenskit.collections.ScoredLongListIterator;
import org.grouplens.lenskit.scored.ScoredId;
import org.grouplens.lenskit.scored.ScoredIdBuilder;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Scored item accumulator with no upper bound.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class UnlimitedScoredItemAccumulator implements ScoredItemAccumulator {
    private ScoredLongList scores;
    private ScoredIdBuilder builder;

    public UnlimitedScoredItemAccumulator() {
        builder = new ScoredIdBuilder();
    }

    @Override
    public boolean isEmpty() {
        return scores == null || scores.isEmpty();
    }

    @Override
    public int size() {
        return scores == null ? 0 : scores.size();
    }

    @Override
    public void put(long item, double score) {
        if (scores == null) {
            scores = new ScoredLongArrayList();
        }
        scores.add(item, score);
    }

    @Override
    public List<ScoredId> finish() {
        if (scores == null) {
            return Collections.emptyList();
        }
        List<ScoredId> ids = Lists.newArrayListWithCapacity(scores.size());
        ScoredLongListIterator it = scores.iterator();
        while (it.hasNext()) {
            long item = it.nextLong();
            double score = it.getScore();
            ids.add(builder.setId(item).setScore(score).build());
        }

        Collections.sort(ids, new Comparator<ScoredId>() {
            @Override
            public int compare(ScoredId o1, ScoredId o2) {
                return DoubleComparators.OPPOSITE_COMPARATOR.compare(o1.getScore(), o2.getScore());
            }
        });

        scores = null;
        return ids;
    }

    @Override
    public MutableSparseVector finishVector() {
        if (scores == null) {
            return new MutableSparseVector();
        }

        // FIXME Don't make a copy here
        MutableSparseVector v = scores.scoreVector().mutableCopy();
        scores = null;
        return v;
    }
}
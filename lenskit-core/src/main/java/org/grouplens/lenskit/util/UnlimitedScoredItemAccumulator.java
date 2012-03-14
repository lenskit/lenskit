/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;

/**
 * Scored item accumulator with no upper bound.
 * @author Michael Ekstrand
 */
public final class UnlimitedScoredItemAccumulator implements ScoredItemAccumulator {
    private ScoredLongArrayList scores;

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
    public ScoredLongList finish() {
        if (scores == null) {
            return new ScoredLongArrayList();
        }

        scores.sort(DoubleComparators.OPPOSITE_COMPARATOR);
        ScoredLongList r = scores;
        scores = null;
        return r;
    }
}

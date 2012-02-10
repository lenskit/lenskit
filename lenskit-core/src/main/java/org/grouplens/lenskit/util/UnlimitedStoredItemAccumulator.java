package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.doubles.DoubleComparators;
import org.grouplens.lenskit.collections.ScoredLongArrayList;
import org.grouplens.lenskit.collections.ScoredLongList;

/**
 * Scored item accumulator with no upper bound.
 * @author Michael Ekstrand
 */
public final class UnlimitedStoredItemAccumulator implements ScoredItemAccumulator {
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
        scores.sort(DoubleComparators.OPPOSITE_COMPARATOR);
        ScoredLongList r = scores;
        scores = null;
        return r;
    }
}

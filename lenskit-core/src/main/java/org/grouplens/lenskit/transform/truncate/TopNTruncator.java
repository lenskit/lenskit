package org.grouplens.lenskit.transform.truncate;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.util.TopNScoredItemAccumulator;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.grouplens.lenskit.vectors.Vectors;

public class TopNTruncator implements VectorTruncator {

    private ThresholdTruncator threshold;
    private int n;

    public TopNTruncator(int n, Threshold threshold) {
        this.n = n;
        if (threshold != null) {
            this.threshold = new ThresholdTruncator(threshold);
        }
    }

    public TopNTruncator(int n) {
        this(n, null);
    }

    @Override
    public void truncate(MutableSparseVector v) {
        if (threshold != null) {
            threshold.truncate(v);
        }

        TopNScoredItemAccumulator accumulator = new TopNScoredItemAccumulator(n);
        for (VectorEntry e : v.fast(VectorEntry.State.SET)) {
            accumulator.put(e.getKey(), e.getValue());
        }
        MutableSparseVector truncated = accumulator.vectorFinish();

        // Unset all elements in 'v' that are not in 'truncated'
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v, truncated)) {
            if (p.getRight() == null) {
                v.unset(p.getLeft());
            }
        }
    }
}

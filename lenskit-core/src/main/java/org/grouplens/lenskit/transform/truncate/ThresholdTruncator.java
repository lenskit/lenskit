package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.transform.threshold.Threshold;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

public class ThresholdTruncator implements VectorTruncator {

    private Threshold threshold;

    public ThresholdTruncator(Threshold threshold) {
        this.threshold = threshold;
    }

    @Override
    public void truncate(MutableSparseVector v) {
        for (VectorEntry e : v.fast()) {
            if (!threshold.retain(e.getValue())) {
                v.unset(e);
            }
        }
    }
}

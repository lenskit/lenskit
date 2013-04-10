package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.transform.threshold.RealThreshold;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestThresholdTruncator {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testTruncate() {
        long[] keys = {1, 2, 3, 4};
        double[] values = {1.0, 2.0, 3.0, 4.0};
        MutableSparseVector v = MutableSparseVector.wrap(keys, values);

        VectorTruncator truncator = new ThresholdTruncator(new RealThreshold(3.5));
        truncator.truncate(v);

        int numSeen = 0;
        for (VectorEntry e : v.fast(VectorEntry.State.SET)) {
            assertThat(e.getKey(), equalTo(4L));
            assertThat(e.getValue(), closeTo(4.0, EPSILON));
            numSeen++;
        }
        assertThat(numSeen, equalTo(1));
    }
}

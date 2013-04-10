package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestTopNTruncator {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testSimpleTruncate() {
        long[] keys = {1, 2, 3, 4, 5};
        double[] values = {1.0, 2.0, 3.0, 4.0, 5.0};
        MutableSparseVector v = MutableSparseVector.wrap(keys, values);

        VectorTruncator truncator = new TopNTruncator(3, null);
        truncator.truncate(v);

        long i = 3;
        for (VectorEntry e : v.fast(VectorEntry.State.SET)) {
            assertThat(e.getKey(), equalTo(i));
            assertThat(e.getValue(), closeTo(i, EPSILON));
            i++;
        }
        assertThat(i, equalTo(6L));
    }
}

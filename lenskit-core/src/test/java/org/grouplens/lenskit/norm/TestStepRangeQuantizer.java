package org.grouplens.lenskit.norm;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * @author Michael Ekstrand
 */
public class TestStepRangeQuantizer {
    @Test
    public void testMakeValues() {
        double[] vals = StepRangeQuantizer.makeValues(0.5, 5.0, 0.5);
        double[] evals = { 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0 };
        assertArrayEquals(evals, vals, 1.0e-6);
    }

    @Test
    public void testHalfStars() {
        Quantizer q = new StepRangeQuantizer(0.5, 5.0, 0.5);
        assertThat(q.getValue(q.apply(4.9)), closeTo(5.0, 1.0e-6));
        assertThat(q.getValue(q.apply(4.7)), closeTo(4.5, 1.0e-6));
        assertThat(q.getValue(q.apply(3.42)), closeTo(3.5, 1.0e-6));
    }
}

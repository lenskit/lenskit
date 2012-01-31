package org.grouplens.lenskit.norm;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

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
}

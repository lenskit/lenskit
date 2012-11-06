package org.grouplens.lenskit.util.statistics;

import org.junit.Before;
import org.junit.Test;

import static org.grouplens.common.test.MoreMatchers.closeTo;
import static org.grouplens.common.test.MoreMatchers.notANumber;
import static org.junit.Assert.assertThat;

/**
 * @author Michael Ekstrand
 */
public class MutualInformationAccumulatorTest {
    MutualInformationAccumulator accum;

    @Before
    public void newAccumulator() {
        accum = new MutualInformationAccumulator(4);
    }

    @Test
    public void testNoData() {
        // if there is no data, entropy is undefined
        assertThat(accum.getMutualInformation(),
                   notANumber());
        assertThat(accum.getV1Entropy(),
                   notANumber());
        assertThat(accum.getV2Entropy(),
                   notANumber());
    }

    /**
     * Several data points, uniform distribution, matched.
     */
    @Test
    public void testUniformFullMI() {
        for (int i = 0; i < 24; i++) {
            accum.count(i % 4, i % 4);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(2));
    }

    /**
     * Several data points, uniform distribution, consistent but not same.
     */
    @Test
    public void testUniformFullMI2() {
        for (int i = 0; i < 24; i++) {
            accum.count(i % 4, 3 - (i + 2) % 4);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(2));
    }

    /**
     * Completely independent variables.
     */
    @Test
    public void testIndependent() {
        for (int i = 0; i < 24; i++) {
            for (int j = 0; j < 24; j++) {
                accum.count(i % 4, j % 4);
            }
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(0));
    }

    /**
     * Half-explanatory variable. For each i, there are two different j values.
     */
    @Test
    public void testHalfExplain() {
        for (int i = 0; i < 32; i++) {
            int x = i % 4;
            int y = (x + (i / 4) % 2) % 4;
            accum.count(x, y);
        }
        assertThat(accum.getV1Entropy(),
                   closeTo(2));
        assertThat(accum.getV2Entropy(),
                   closeTo(2));
        assertThat(accum.getMutualInformation(),
                   closeTo(1));
    }
}

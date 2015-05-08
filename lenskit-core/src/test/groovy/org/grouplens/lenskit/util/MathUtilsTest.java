package org.grouplens.lenskit.util;

import net.java.quickcheck.collection.Pair;
import org.junit.Test;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.somePairs;
import static net.java.quickcheck.generator.PrimitiveGenerators.doubles;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MathUtilsTest {
    @Test
    public void testZeroIsZero() {
        assertThat(MathUtils.isZero(0), equalTo(true));
    }

    @Test
    public void testZeroIsZeroWithEpsilon() {
        assertThat(MathUtils.isZero(0, 1.0e-6), equalTo(true));
    }

    @Test
    public void testOneIsNotZero() {
        assertThat(MathUtils.isZero(1), equalTo(false));
    }

    @Test
    public void testOneIsNotZeroWithEpsilon() {
        assertThat(MathUtils.isZero(1, 1.0e-6), equalTo(false));
    }

    @Test
    public void testSmallIsZero() {
        assertThat(MathUtils.isZero(1.0e-6, 1.0e-5),
                   equalTo(true));
    }

    @Test
    public void testManyNumbersMightBeZero() {
        for (Pair<Double,Double> pair: somePairs(doubles(-10, 10), doubles(0, 2))) {
            assertThat(MathUtils.isZero(pair.getFirst(), pair.getSecond()),
                       equalTo(Math.abs(pair.getFirst()) < pair.getSecond()));
        }
    }
}

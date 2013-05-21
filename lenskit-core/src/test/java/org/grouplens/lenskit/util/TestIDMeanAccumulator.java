package org.grouplens.lenskit.util;

import org.grouplens.lenskit.vectors.SparseVector;
import org.junit.Before;
import org.junit.Test;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TestIDMeanAccumulator {
    private static final double EPSILON = 1.0e-5;
    IDMeanAccumulator acc;
    @Before
    public void createAccumulator() {
        acc = new IDMeanAccumulator();
    }

    @Test
    public void testNone() {
        assertThat(acc.globalMean(), notANumber());
        assertThat(acc.idMeans().isEmpty(),
                   equalTo(true));
        assertThat(acc.idMeanOffsets().isEmpty(),
                   equalTo(true));
    }

    @Test
    public void testSingleValue() {
        acc.add(5, Math.PI);
        assertThat(acc.globalMean(),
                   closeTo(Math.PI, EPSILON));
        SparseVector v = acc.idMeans();
        assertThat(v.size(), equalTo(1));
        assertThat(v.get(5), closeTo(Math.PI, EPSILON));
    }

    @Test
    public void testSeparateValues() {
        acc.add(5, Math.PI);
        acc.add(17, Math.E);
        assertThat(acc.globalMean(),
                   closeTo((Math.PI + Math.E) / 2, EPSILON));
        SparseVector v = acc.idMeans();
        assertThat(v.size(), equalTo(2));
        assertThat(v.get(5), closeTo(Math.PI, EPSILON));
        assertThat(v.get(17), closeTo(Math.E, EPSILON));
    }

    @Test
    public void testMultipleValues() {
        acc.add(5, 3);
        acc.add(17, 4);
        acc.add(17, 5);
        acc.add(5, 3);
        acc.add(3, 2);
        assertThat(acc.globalMean(),
                   closeTo(3.4, EPSILON));
        SparseVector v = acc.idMeans();
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(5), closeTo(3, EPSILON));
        assertThat(v.get(17), closeTo(4.5, EPSILON));
        assertThat(v.get(3), closeTo(2, EPSILON));
    }

    @Test
    public void testMeanOffsets() {
        acc.add(5, 3);
        acc.add(17, 4);
        acc.add(17, 5);
        acc.add(5, 3);
        acc.add(3, 2);
        assertThat(acc.globalMean(),
                   closeTo(3.4, EPSILON));
        SparseVector v = acc.idMeanOffsets();
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(5), closeTo(-0.4, EPSILON));
        assertThat(v.get(17), closeTo(1.1, EPSILON));
        assertThat(v.get(3), closeTo(-1.4, EPSILON));
    }

    @Test
    public void testDampedMeanOffsets() {
        acc.add(5, 3);
        acc.add(17, 4);
        acc.add(17, 5);
        acc.add(5, 3);
        acc.add(3, 2);
        assertThat(acc.globalMean(),
                   closeTo(3.4, EPSILON));
        SparseVector v = acc.idMeanOffsets(1);
        assertThat(v.size(), equalTo(3));
        assertThat(v.get(5), closeTo(-0.26666666667, EPSILON));
        assertThat(v.get(17), closeTo(0.73333333333, EPSILON));
        assertThat(v.get(3), closeTo(-0.7, EPSILON));
    }
}
package org.grouplens.lenskit.vectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TestFastUnion {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testIdentical() {
        long[] keys1 = {1, 2, 3, 4};
        long[] keys2 = Arrays.copyOf(keys1, keys1.length);
        double[] values1 = {1.0, 2.0, 3.0, 4.0};
        double[] values2 = Arrays.copyOf(values1, values1.length);

        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v1, v2)) {
            assertThat(p.getLeft().getKey(), equalTo(i));
            assertThat(p.getLeft().getValue(), closeTo(i, EPSILON));

            assertThat(p.getRight().getKey(), equalTo(i));
            assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            i++;
        }
        assertThat(i, equalTo(5L));
    }

    @Test
    public void testEmptyLeft() {
        long[] keys = {1, 2, 3, 4};
        double[] values = {1.0, 2.0, 3.0, 4.0};
        SparseVector v1 = new MutableSparseVector();
        SparseVector v2 = MutableSparseVector.wrap(keys, values);

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v1,v2)) {
            assertThat(p.getLeft(), nullValue());

            assertThat(p.getRight().getKey(), equalTo(i));
            assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            i++;
        }

        assertThat(i, equalTo(5L));
    }

    @Test
    public void testEmptyRight() {
        long[] keys = {1, 2, 3, 4};
        double[] values = {1.0, 2.0, 3.0, 4.0};
        SparseVector v1 = MutableSparseVector.wrap(keys, values);
        SparseVector v2 = new MutableSparseVector();

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v1,v2)) {
            assertThat(p.getLeft().getKey(), equalTo(i));
            assertThat(p.getLeft().getValue(), closeTo(i, EPSILON));

            assertThat(p.getRight(), nullValue());
            i++;
        }
        assertThat(i, equalTo(5L));
    }

    @Test
    public void testOverlap() {
        long[] keys1 = {1, 2, 3};
        double[] values1 = {1.0, 2.0, 3.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {3, 4, 5};
        double[] values2 = {3.0, 4.0, 5.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v1, v2)) {
            if (i < 3) {
                assertThat(p.getLeft().getKey(), equalTo(i));
                assertThat(p.getLeft().getValue(), closeTo(i, EPSILON));

                assertThat(p.getRight(), nullValue());
            } else if (i == 3) {
                assertThat(p.getLeft().getKey(), equalTo(i));
                assertThat(p.getLeft().getValue(), closeTo(i, EPSILON));

                assertThat(p.getRight().getKey(), equalTo(i));
                assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            } else {
                assertThat(p.getLeft(), nullValue());

                assertThat(p.getRight().getKey(), equalTo(i));
                assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            }

            i++;
        }
        assertThat(i, equalTo(6L));
    }

    @Test
    public void testNoOverlap() {
        long[] keys1 = {1, 2, 3};
        double[] values1 = {1.0, 2.0, 3.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {4, 5, 6};
        double[] values2 = {4.0, 5.0, 6.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastUnion(v1, v2)) {
            if (i < 4) {
                assertThat(p.getLeft().getKey(), equalTo(i));
                assertThat(p.getLeft().getValue(), closeTo(i,  EPSILON));

                assertThat(p.getRight(), nullValue());
            } else {
                assertThat(p.getLeft(), nullValue());

                assertThat(p.getRight().getKey(), equalTo(i));
                assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            }
            i++;
        }
        assertThat(i, equalTo(7L));
    }
}
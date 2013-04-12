/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
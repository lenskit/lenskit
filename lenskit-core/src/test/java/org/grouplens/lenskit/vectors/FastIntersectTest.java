/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
import java.util.Iterator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FastIntersectTest {

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
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastIntersect(v1, v2)) {
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

        assertThat(Vectors.fastIntersect(v1, v2),
                   emptyIterable());
    }

    @Test
    public void testEmptyRight() {
        long[] keys = {1, 2, 3, 4};
        double[] values = {1.0, 2.0, 3.0, 4.0};
        SparseVector v1 = MutableSparseVector.wrap(keys, values);
        SparseVector v2 = new MutableSparseVector();

        assertThat(Vectors.fastIntersect(v1, v2),
                   emptyIterable());
    }

    @Test
    public void testOverlap() {
        long[] keys1 = {1, 2, 3};
        double[] values1 = {1.0, 2.0, 3.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {3, 4, 5};
        double[] values2 = {4.0, 5.0, 6.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        Iterator<Pair<VectorEntry,VectorEntry>> iter = Vectors.fastIntersect(v1, v2).iterator();
        Pair<VectorEntry,VectorEntry> p = iter.next();
        assertThat(p.getLeft().getKey(), equalTo(3L));
        assertThat(p.getRight().getKey(), equalTo(3L));
        assertThat(p.getLeft().getValue(), closeTo(3, EPSILON));
        assertThat(p.getRight().getValue(), closeTo(4, EPSILON));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testNoOverlap() {
        long[] keys1 = {1, 2, 3};
        double[] values1 = {1.0, 2.0, 3.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {4, 5, 6};
        double[] values2 = {4.0, 5.0, 6.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        assertThat(Vectors.fastIntersect(v1, v2),
                   emptyIterable());
    }

    @Test
    public void testInterleave1() {
        long[] keys1 = {1, 3, 5};
        double[] values1 = {1.0, 3.0, 5.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {2, 4, 6};
        double[] values2 = {2.0, 4.0, 6.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        assertThat(Vectors.fastIntersect(v1, v2), emptyIterable());
    }

    @Test
    public void testInterleave2() {
        long[] keys1 = {1, 3, 5};
        double[] values1 = {1.0, 3.0, 5.0};
        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);

        long[] keys2 = {2, 3, 4};
        double[] values2 = {2.0, 3.2, 4.0};
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        Iterator<Pair<VectorEntry,VectorEntry>> iter = Vectors.fastIntersect(v1, v2).iterator();
        Pair<VectorEntry,VectorEntry> p = iter.next();
        assertThat(p.getLeft().getKey(), equalTo(3L));
        assertThat(p.getRight().getKey(), equalTo(3L));
        assertThat(p.getLeft().getValue(), closeTo(3, EPSILON));
        assertThat(p.getRight().getValue(), closeTo(3.2, EPSILON));
        assertThat(iter.hasNext(), equalTo(false));
    }

    @Test
    public void testCommon() {
        long[] keys1 = {1, 2, 3, 5};
        long[] keys2 = {1, 2, 4, 5};
        double[] values1 = {1.0, 2.0, 3.0, 4.0};
        double[] values2 = Arrays.copyOf(values1, values1.length);

        SparseVector v1 = MutableSparseVector.wrap(keys1, values1);
        SparseVector v2 = MutableSparseVector.wrap(keys2, values2);

        long i = 1;
        for (Pair<VectorEntry,VectorEntry> p : Vectors.fastIntersect(v1, v2)) {
            if (i < 3) {
                assertThat(p.getLeft().getKey(), equalTo(i));
                assertThat(p.getLeft().getValue(), closeTo(i, EPSILON));

                assertThat(p.getRight().getKey(), equalTo(i));
                assertThat(p.getRight().getValue(), closeTo(i, EPSILON));
            } else {
                assertThat(p.getLeft().getKey(), equalTo(5L));
                assertThat(p.getRight().getKey(), equalTo(5L));
                assertThat(p.getLeft().getValue(), equalTo(4.0));
                assertThat(p.getRight().getValue(), equalTo(4.0));
            }
            i++;
        }
        assertThat(i, equalTo(4L));
    }
}
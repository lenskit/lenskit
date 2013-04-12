package org.grouplens.lenskit.vectors;

import org.junit.Test;

import static org.grouplens.lenskit.vectors.SparseVectorTestCommon.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class TestVector {
    final Vector empty = Vector.wrap(new double[0]);
    final Vector single = Vector.wrap(new double[]{3.5});
    final Vector v1 = Vector.wrap(new double[]{1,3,5});
    final Vector v1c = Vector.wrap(new double[]{1,3,5});
    final Vector v2 = Vector.wrap(new double[]{2,3,4});

    @Test
    public void testDim() {
        assertThat(empty.dim(), equalTo(0));
        assertThat(single.dim(), equalTo(1));
        assertThat(v2.dim(), equalTo(3));
    }

    @Test
    public void testGet() {
        assertThat(single.get(0), closeTo(3.5));
        assertThat(v1.get(0), closeTo(1));
        assertThat(v1.get(1), closeTo(3));
        assertThat(v1.get(2), closeTo(5));
    }

    @Test
    public void testSum() {
        assertThat(empty.sum(), closeTo(0));
        assertThat(single.sum(), closeTo(3.5));
        assertThat(v1.sum(), closeTo(9));
        assertThat(v2.sum(), closeTo(9));
    }

    @Test
    public void testNorm() {
        assertThat(empty.norm(), closeTo(0));
        assertThat(single.norm(), closeTo(3.5));
        assertThat(v1.norm(), closeTo(5.9160798));
    }

    @Test
    public void testDot() {
        assertThat(v1.dot(v2), closeTo(2 + 9 + 20));
        assertThat(v1.dot(v1c), closeTo(v1.norm() * v1c.norm()));
        try {
            v1.dot(single);
            fail("dot product with different-dimensioned vector should fail");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    @Test
    public void testEquals() {
        assertThat(v1.equals(v1c), equalTo(true));
        assertThat(v1.equals(v2), equalTo(false));
        assertThat(v1.equals(v1), equalTo(true));
        assertThat(v1.equals(single), equalTo(false));
        assertThat(v1.equals(empty), equalTo(false));
        assertThat(v1.equals("foo"), equalTo(false));
    }

    @Test
    public void testHashCode() {
        assertThat(v1.hashCode(), equalTo(v1c.hashCode()));
        assertThat(empty.hashCode(), not(equalTo(v1c.hashCode())));
    }
}

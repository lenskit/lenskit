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
package org.grouplens.lenskit.transform.truncate;

import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import org.junit.Test;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class TopNTruncatorTest {

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

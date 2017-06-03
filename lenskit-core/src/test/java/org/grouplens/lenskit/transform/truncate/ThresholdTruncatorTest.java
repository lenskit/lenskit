/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.grouplens.lenskit.transform.threshold.RealThreshold;
import org.junit.Test;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class ThresholdTruncatorTest {

    private static final double EPSILON = 1.0e-6;

    @Test
    public void testTruncate() {
        long[] keys = {1, 2, 3, 4};
        double[] values = {1.0, 2.0, 3.0, 4.0};
        Long2DoubleSortedArrayMap v = Long2DoubleSortedArrayMap.wrapUnsorted(keys, values);

        VectorTruncator truncator = new ThresholdTruncator(new RealThreshold(3.5));
        Long2DoubleMap v2 = truncator.truncate(v);

        int numSeen = 0;
        for (Long2DoubleMap.Entry e: v2.long2DoubleEntrySet()) {
            assertThat(e.getLongKey(), equalTo(4L));
            assertThat(e.getDoubleValue(), closeTo(4.0, EPSILON));
            numSeen++;
        }
        assertThat(numSeen, equalTo(1));
    }
}

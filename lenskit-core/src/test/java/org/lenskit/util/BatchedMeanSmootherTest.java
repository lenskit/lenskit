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
package org.lenskit.util;

import org.junit.Test;

import static org.grouplens.lenskit.util.test.ExtraMatchers.notANumber;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class BatchedMeanSmootherTest {
    @Test
    public void testEmpty() {
        BatchedMeanSmoother smooth = new BatchedMeanSmoother(10);
        assertThat(smooth.currentAverage(),
                   notANumber());
    }

    @Test
    public void testAddOne() {
        BatchedMeanSmoother smooth = new BatchedMeanSmoother(10);
        smooth.addBatch(10, 5.0);
        assertThat(smooth.currentAverage(),
                   equalTo(0.5));
    }

    @Test
    public void testAddTwo() {
        BatchedMeanSmoother smooth = new BatchedMeanSmoother(10);
        smooth.addBatch(10, 5.0);
        smooth.addBatch(5, 5.0);
        assertThat(smooth.currentAverage(),
                   closeTo(0.666666667, 1.0e-6));
    }
}

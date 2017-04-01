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
package org.lenskit.eval.traintest;


import org.lenskit.api.Recommender;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Measures the performance of a single experimental condition.  Condition evaluators must be thread-safe, allowing
 * multiple users to be measured in parallel.
 */
public interface ConditionEvaluator {
    /**
     * Measure the performance for a single user.
     * @param rec The recommender to use.
     * @param testUser The user to test.
     * @return The per-user performance measurements.
     */
    @Nonnull
    Map<String,Object> measureUser(Recommender rec, TestUser testUser);

    /**
     * Finish measuring the performance for the algorithm and data set.
     * @return The aggregate performance measurements.
     */
    @Nonnull
    Map<String,Object> finish();
}

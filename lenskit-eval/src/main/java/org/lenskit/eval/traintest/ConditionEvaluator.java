/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

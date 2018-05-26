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
package org.lenskit.eval.traintest.recommend;

import org.lenskit.api.Recommender;
import org.lenskit.api.ResultList;
import org.lenskit.eval.traintest.TestUser;
import org.lenskit.eval.traintest.metrics.MetricResult;
import org.lenskit.eval.traintest.metrics.TypedMetricResult;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Intermediate class for top-N metrics that only depend on the list of recommended items, not their details.
 * Metrics extending this class will implement the {@link #measureUserRecList(Recommender, TestUser, int, List, Object)} method
 * instead of {@link #measureUser(Recommender, TestUser, int, ResultList, Object)}.  The recommend eval task uses this
 * subclass to improve efficiency when results are not used in the evaluation.
 *
 * @param <X> The accumulator type.
 */
public abstract class ListOnlyTopNMetric<X> extends TopNMetric<X> {
    protected ListOnlyTopNMetric(List<String> labels, List<String> aggLabels) {
        super(labels, aggLabels);
    }

    protected ListOnlyTopNMetric(Class<? extends TypedMetricResult> resType, Class<? extends TypedMetricResult> aggType) {
        super(resType, aggType);
    }

    protected ListOnlyTopNMetric(Class<? extends TypedMetricResult> resType, Class<? extends TypedMetricResult> aggType, String suffix) {
        super(resType, aggType, suffix);
    }

    @Nonnull
    @Override
    public final MetricResult measureUser(Recommender rec, TestUser user, int targetLength, ResultList recommendations, X context) {
        return measureUserRecList(rec, user, targetLength,
                                  LongUtils.asLongList(recommendations.idList()),
                                  context);
    }

    /**
     * Measurement method that only uses the recommended list, without any scores or details.
     *
     * **Thread Safety:** This method may be called concurrently by multiple threads with the same recommender and
     * context.
     *
     * @param rec The recommender used to recommend for this user.
     * @param user The user.
     * @param targetLength The target list length.
     * @param recommendations The list of recommendations.
     * @param context The context.
     * @return The results of measuring this user.
     */
    @Nonnull
    public abstract MetricResult measureUserRecList(Recommender rec, TestUser user, int targetLength, List<Long> recommendations, X context);
}

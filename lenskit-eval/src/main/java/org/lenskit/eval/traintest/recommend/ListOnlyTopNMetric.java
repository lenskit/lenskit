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
package org.lenskit.eval.traintest.recommend;

import it.unimi.dsi.fastutil.longs.LongList;
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
 * Metrics extending this class will implement the {@link #measureUser(Recommender, TestUser, int, LongList, Object)} method
 * instead of {@link #measureUser(TestUser, int, ResultList, Object)}.  The recommend eval task uses this
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
        return measureUser(rec, user, targetLength,
                           LongUtils.asLongList(recommendations.idList()),
                           context);
    }

    /**
     * Measurement method that only uses the recommend list.
     *
     * @param rec The recommender used to recommend for this user.
     * @param user The user.
     * @param targetLength The target list length.
     * @param recommendations The list of recommendations.
     * @param context The context.
     * @return The results of measuring this user.
     */
    @Nonnull
    public abstract MetricResult measureUser(Recommender rec, TestUser user, int targetLength, LongList recommendations, X context);
}

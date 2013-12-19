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
package org.grouplens.lenskit.eval.metrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.eval.traintest.TrainTestEvalTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base implementation of {@link TestUserMetric}.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public abstract class AbstractTestUserMetric
        extends AbstractMetric<TrainTestEvalTask>
        implements TestUserMetric {
    /**
     * Make a user result row. This expands it to the length of the user columns, inserting
     * {@code null}s as needed.
     * @return The result row, the same length as {@link #getUserColumnLabels()}.
     */
    protected List<Object> userRow(Object... results) {
        int len = getUserColumnLabels().size();
        Preconditions.checkArgument(results.length <= len, "too many results");;
        List<Object> row = Lists.newArrayListWithCapacity(len);
        Collections.addAll(row, results);
        while (row.size() < len) {
            row.add(null);
        }
        return row;
    }

    /**
     * Make a final aggregate result row. This expands it to the length of the columns, inserting
     * {@code null}s as needed.
     * @return The result row, the same length as {@link #getColumnLabels()}.
     */
    protected List<Object> finalRow(Object... results) {
        int len = getColumnLabels().size();
        Preconditions.checkArgument(results.length <= len, "too many results");;
        List<Object> row = Lists.newArrayListWithCapacity(len);
        Collections.addAll(row, results);
        while (row.size() < len) {
            row.add(null);
        }
        return row;
    }
}

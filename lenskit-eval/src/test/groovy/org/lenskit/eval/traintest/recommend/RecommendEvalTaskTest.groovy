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
package org.lenskit.eval.traintest.recommend

import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

public class RecommendEvalTaskTest {
    RecommendEvalTask task

    @Before
    void createTask() {
        task = new RecommendEvalTask()
    }

    @Test
    void testColumns() {
        task.topNMetrics.clear()
        task.addMetric(new TopNMRRMetric())
        assertThat(task.userColumns,
                   containsInAnyOrder("Rank", "RecipRank"))
        assertThat(task.globalColumns,
                   contains("MRR"))
    }

    @Test
    void testColumnsWithPrefix() {
        task.topNMetrics.clear()
        task.labelPrefix = 'Foo'
        task.addMetric(new TopNMRRMetric())
        assertThat(task.userColumns,
                   containsInAnyOrder("Foo.Rank", "Foo.RecipRank"))
        assertThat(task.globalColumns,
                   contains("Foo.MRR"))
    }
}

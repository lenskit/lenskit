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
                   contains("MRR", "MRR.OfGood"))
    }

    @Test
    void testColumnsWithPrefix() {
        task.topNMetrics.clear()
        task.labelPrefix = 'Foo'
        task.addMetric(new TopNMRRMetric())
        assertThat(task.userColumns,
                   containsInAnyOrder("Foo.Rank", "Foo.RecipRank"))
        assertThat(task.globalColumns,
                   contains("Foo.MRR", "Foo.MRR.OfGood"))
    }
}

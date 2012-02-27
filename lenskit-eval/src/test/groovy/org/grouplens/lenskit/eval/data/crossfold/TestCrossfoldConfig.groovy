package org.grouplens.lenskit.eval.data.crossfold

import static org.junit.Assert.*
import static org.hamcrest.Matchers.*
import org.grouplens.lenskit.eval.config.ConfigTestBase
import org.junit.Test
import org.grouplens.lenskit.eval.data.CSVDataSource

/**
 * Test crossfold configuration
 * @author Michael Ekstrand
 */
class TestCrossfoldConfig extends ConfigTestBase {
    @Test
    void testBasicCrossfold() {
        def obj = eval {
            crossfold("ml-100k") {
                source "ml-100k.csv"
                partitions 10
                holdout 0.5
                order RandomOrder
            }
        }
        def cf = obj as CrossfoldSplit
        assertThat(cf.name, equalTo("ml-100k"))
        assertThat(cf.source, instanceOf(CSVDataSource))
        assertThat(cf.partitionCount, equalTo(10))
        assertThat(cf.holdout.order, instanceOf(RandomOrder))
        assertThat(cf.holdout.partitionMethod, instanceOf(FractionPartition))
        assertThat(cf.holdout.partitionMethod.fraction, closeTo(0.5, 1.0e-6))
    }
}

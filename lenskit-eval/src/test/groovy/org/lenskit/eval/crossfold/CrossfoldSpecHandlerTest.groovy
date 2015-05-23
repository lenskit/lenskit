package org.lenskit.eval.crossfold

import org.grouplens.lenskit.specs.SpecificationContext
import org.grouplens.lenskit.util.test.MiscBuilders
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

/**
 * Created by michael on 5/22/2015.
 */
class CrossfoldSpecHandlerTest extends GroovyTestCase {
    @Test
    public void testConfigureCrossfolder() {
        def cfg = MiscBuilders.configObj {
            name "asdf"
            source {
                type "csvfile"
                file "ratings.csv"
            }
            partitions 12
        }
        def cf = SpecificationContext.create().build(Crossfolder, cfg)
        assertThat cf.name, equalTo("asdf")
        assertThat cf.partitionCount, equalTo(12)
    }
}

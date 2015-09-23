package org.lenskit.gradle.delegates

import org.gradle.util.ConfigureUtil
import org.junit.Before
import org.junit.Test
import org.lenskit.specs.AbstractSpec
import org.lenskit.specs.data.TextDataSourceSpec
import org.lenskit.specs.eval.PredictEvalTaskSpec

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class SpecDelegateTest {
    public <T extends AbstractSpec> T configure(Class<T> cls, Closure block) {
        def spec = cls.newInstance()
        def delegate = new SpecDelegate(spec)
        ConfigureUtil.configure(block, delegate)
        return spec
    }

    @Test
    public void testTextData() {
        def spec = configure(TextDataSourceSpec) {
            delimiter '::'
            file 'data/ratings.csv'
        }
        assertThat spec.delimiter, equalTo('::')
    }
}

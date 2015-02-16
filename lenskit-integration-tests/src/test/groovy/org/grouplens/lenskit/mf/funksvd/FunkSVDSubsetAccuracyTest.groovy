package org.grouplens.lenskit.mf.funksvd

import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.data.dao.ItemDAO
import org.grouplens.lenskit.test.ML100KTestSuite

class FunkSVDSubsetAccuracyTest extends FunkSVDAccuracyTest {
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        super.configureAlgorithm(config)
        config.bind(ItemDAO).toProvider(ML100KTestSuite.SubsetProvider)
    }
}

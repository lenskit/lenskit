package org.grouplens.lenskit.knn.item

import org.grouplens.lenskit.core.LenskitConfiguration
import org.grouplens.lenskit.data.dao.ItemDAO
import org.grouplens.lenskit.test.ML100KTestSuite

class ItemItemSubsetAccuracyTest extends ItemItemAccuracyTest {
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        super.configureAlgorithm(config)
        config.bind(ItemDAO).toProvider(ML100KTestSuite.SubsetProvider)
    }
}

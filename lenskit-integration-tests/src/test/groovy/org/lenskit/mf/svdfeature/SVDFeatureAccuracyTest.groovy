package org.lenskit.mf.svdfeature

import org.lenskit.LenskitConfiguration

import org.lenskit.api.ItemScorer
import org.lenskit.config.ConfigHelpers
import org.lenskit.data.entities.CommonTypes
import org.lenskit.mf.funksvd.FeatureCount
import org.lenskit.mf.funksvd.FunkSVDAccuracyTest;


public class SVDFeatureAccuracyTest extends FunkSVDAccuracyTest {
    @SuppressWarnings("unchecked")
    @Override
    protected void configureAlgorithm(LenskitConfiguration config) {
        ConfigHelpers.configure(config) {
            bind ItemScorer to SVDFeatureItemScorer
            set SVDFeatureEntityType to CommonTypes.RATING
            set FeatureCount to 25
        }
    }
}

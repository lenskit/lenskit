package org.lenskit.mf.hmmsvd;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelBuildTest {
    @Test
    public void testModelBuild() throws FileNotFoundException, IOException {
        String path = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-clkrat.te";
        int numBiases = 38529;
        int numFactors = 38528;
        int dim = 20;
        HmmSVDFeatureInstanceDAO dao = new HmmSVDFeatureInstanceDAO(new File(path), " ");
        HmmSVDFeatureModelBuilder modelBuilder = new HmmSVDFeatureModelBuilder(24, numBiases, numFactors, dim, dao);
        HmmSVDFeatureModel model = modelBuilder.build();
    }
}

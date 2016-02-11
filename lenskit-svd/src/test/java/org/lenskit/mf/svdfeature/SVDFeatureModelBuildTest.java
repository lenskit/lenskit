package org.lenskit.mf.svdfeature;

import org.junit.Test;
import org.lenskit.solver.objective.LogisticLoss;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuildTest {
    @Test
    public void testModelBuild() throws FileNotFoundException, IOException {
        String path = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat.te";
        int numBiases = 38529;
        int numFactors = 38528;
        int dim = 20;
        SVDFeatureInstanceDAO dao = new SVDFeatureInstanceDAO(new File(path), " ");
        LogisticLoss loss = new LogisticLoss();
        SVDFeatureModelBuilder modelBuilder = new SVDFeatureModelBuilder(numBiases, numFactors, dim, dao, loss);
        SVDFeatureModel model = modelBuilder.build();
    }
}

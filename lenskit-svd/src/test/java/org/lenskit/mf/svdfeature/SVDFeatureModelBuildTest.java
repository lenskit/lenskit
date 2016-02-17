package org.lenskit.mf.svdfeature;

import org.junit.Test;
import org.lenskit.solver.objective.LogisticLoss;

import java.io.*;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuildTest {
    @Test
    public void testModelBuild() throws FileNotFoundException, IOException {
        String train = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat.te";
        int numBiases = 38544;
        int numFactors = 38543;
        int dim = 20;
        SVDFeatureInstanceDAO dao = new SVDFeatureInstanceDAO(new File(train), " ");
        LogisticLoss loss = new LogisticLoss();
        SVDFeatureModelBuilder modelBuilder = new SVDFeatureModelBuilder(numBiases, numFactors,
                dim, dao, loss);
        SVDFeatureModel model = modelBuilder.build();
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat.model";
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(modelFile));
        fout.writeObject(model);
        fout.close();
    }
}

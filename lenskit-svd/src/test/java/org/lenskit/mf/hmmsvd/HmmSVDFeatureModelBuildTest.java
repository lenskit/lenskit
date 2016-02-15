package org.lenskit.mf.hmmsvd;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelBuildTest {
    @Test
    public void testModelBuild() throws FileNotFoundException, IOException {
        String train = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.tr";
        int numBiases = 38544;
        int numFactors = 38543;
        int dim = 20;
        HmmSVDFeatureInstanceDAO trDao = new HmmSVDFeatureInstanceDAO(new File(train), " ");
        HmmSVDFeatureModelBuilder modelBuilder = new HmmSVDFeatureModelBuilder(24, numBiases,
                numFactors, dim, trDao);
        HmmSVDFeatureModel model = modelBuilder.build();
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.model2";
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(modelFile));
        fout.writeObject(model);
        fout.close();
    }
}

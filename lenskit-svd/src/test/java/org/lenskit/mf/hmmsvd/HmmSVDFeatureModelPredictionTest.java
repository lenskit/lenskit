package org.lenskit.mf.hmmsvd;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelPredictionTest {
    @Test
    pubic void predictionTest() throws FileNotFoundException, IOException {
        String testFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat-feas.te";
        String predFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat-feas.te.pred";
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.model";

        ObjectInputStream fin = new ObjectInputStream(new FileInputStream(modelFile));
        HmmSVDFeatureModel model = (HmmSVDFeatureModel)(fin.readObject());
        fin.close();
        SVDFeatureModel svdFea = model.getSVDFeatureModel();

        SVDFeatureInstanceDAO teDao = new SVDFeatureInstanceDAO(new File(testFile), " ");
        BufferedWriter fout = new BufferedWriter(new FileWriter(predFile));
        SVDFeatureInstance ins = null;
        do {
            ins = dao.getNextInstance();
            double prob = svdFea.predict(ins, true);
            fout.write(Double.toString(prob) + "\n");
        } while (ins != null)
        fout.close();
    }
}

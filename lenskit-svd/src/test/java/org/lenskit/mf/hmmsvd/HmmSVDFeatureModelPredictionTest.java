package org.lenskit.mf.hmmsvd;

import org.junit.Test;
import org.lenskit.mf.svdfeature.SVDFeatureInstance;
import org.lenskit.mf.svdfeature.SVDFeatureInstanceDAO;
import org.lenskit.mf.svdfeature.SVDFeatureModel;

import java.io.*;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelPredictionTest {
    @Test
    public void predictionTest() throws IOException, ClassNotFoundException {
        String testFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/svdfea11-clkrat.te";
        String predFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-clkrat.te.pred";
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.model";

        ObjectInputStream fin = new ObjectInputStream(new FileInputStream(modelFile));
        HmmSVDFeatureModel model = (HmmSVDFeatureModel)(fin.readObject());
        fin.close();
        SVDFeatureModel svdFea = model.getSVDFeatureModel();

        SVDFeatureInstanceDAO teDao = new SVDFeatureInstanceDAO(new File(testFile), " ");
        BufferedWriter fout = new BufferedWriter(new FileWriter(predFile));
        SVDFeatureInstance ins = teDao.getNextInstance();
        while (ins != null) {
            double prob = svdFea.predict(ins, true);
            fout.write(Double.toString(prob) + "\n");
            ins = teDao.getNextInstance();
        }
        fout.close();
    }
}

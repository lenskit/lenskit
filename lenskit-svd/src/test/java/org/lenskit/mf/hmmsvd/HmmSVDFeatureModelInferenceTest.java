package org.lenskit.mf.hmmsvd;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelInferenceTest {

    @Test
    pubic void inferenceTest() throws FileNotFoundException, IOException {
        String testFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/labeled-hmmsvdfeature-input.te";
        String predFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/labeled-hmmsvdfeature-input.te.pred";
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.model";

        ObjectInputStream fin = new ObjectInputStream(new FileInputStream(modelFile));
        HmmSVDFeatureModel model = (HmmSVDFeatureModel)(fin.readObject());
        fin.close();

        HmmSVDFeatureInstanceDAO teDao = new HmmSVDFeatureInstanceDAO(new File(testFile), " ");
        BufferedWriter fout = new BufferedWriter(new FileWriter(predFile));
        HmmSVDFeatureInstance ins = null;
        do {
            ins = dao.getNextInstance();
            ArrayList<RealVector> gamma = new ArrayList<>(ins.numObs);
            ArrayList<ArrayList<RealVector>> xi = new ArrayList<>(ins.numObs - 1);
            model.stochasticInference(ins, gamma, xi);
            RealVector sum = MatrixUtils.createRealVector(new double[ins.numObs]);
            for (int i=0; i<ins.numObs; i++) {
                sum.combineToSelf(gamma.get(i));
            }
            String[] line = new String[ins.numPos];
            for (int i=0; i<ins.numPos; i++) {
                line[i] = Double.toString(sum.getEntry(i));
            }
            fout.write(line.join(" ") + "\n");
        } while (ins != null)
        fout.close();
    }
}

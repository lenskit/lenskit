package org.lenskit.mf.hmmsvd;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;

import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelInferenceTest {

    @Test
    public void inferenceTest() throws IOException, ClassNotFoundException {
        String testFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/labeled-hmmsvdfeature-input.te";
        String predFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/labeled-hmmsvdfeature-input.te.pred";
        String modelFile = "/home/qian/Study/pyml/NoisyNegativeImplicitFeedback/data/hmmsvd11-withlab-clkrat.model";

        ObjectInputStream fin = new ObjectInputStream(new FileInputStream(modelFile));
        HmmSVDFeatureModel model = (HmmSVDFeatureModel)(fin.readObject());
        fin.close();

        HmmSVDFeatureInstanceDAO teDao = new HmmSVDFeatureInstanceDAO(new File(testFile), " ");
        BufferedWriter fout = new BufferedWriter(new FileWriter(predFile));
        HmmSVDFeatureInstance ins = teDao.getNextInstance();
        while(ins != null) {
            ArrayList<RealVector> gamma = new ArrayList<>(ins.numObs);
            ArrayList<ArrayList<RealVector>> xi = new ArrayList<>(ins.numObs - 1);
            model.stochasticInference(ins, gamma, xi);
            RealVector sum = MatrixUtils.createRealVector(new double[ins.numPos]);
            for (int i=0; i<ins.numObs; i++) {
                sum.combineToSelf(1.0, 1.0, gamma.get(i));
            }
            String[] line = new String[ins.numPos];
            for (int i=0; i<ins.numPos; i++) {
                line[i] = Double.toString(sum.getEntry(i));
            }
            fout.write(StringUtils.join(line, " ") + "\n");
            ins = teDao.getNextInstance();
        }
        fout.close();
    }
}

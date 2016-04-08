package org.lenskit.mf.svdfeature;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import org.lenskit.solver.*;
import org.lenskit.util.keys.ObjectKeyIndex;
import org.lenskit.util.keys.SynchronizedIndexSpace;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel extends AbstractLearningModel {
    private SynchronizedIndexSpace indexSpace;
    private ObjectiveFunction objectiveFunction;
    private int factDim;
    private int biasSize;
    private int factSize;

    public SVDFeatureModel(int biasSize, int factSize, int factDim, ObjectiveFunction objectiveFunction) {
        super();
        this.biasSize = biasSize;
        this.factSize = factSize;
        this.factDim = factDim;
        this.objectiveFunction = objectiveFunction;
        this.variableSpace.requestScalarVar("biases", this.biasSize, 0, false, false);
        this.variableSpace.requestVectorVar("factors", this.factSize, this.factDim, 0, true, false);
    }

    /* SVDFeatureModel must fulfill transforming raw data to svdfeature instance because
        1. indexSpace is together with a model
        2. during prediction online, raw data is given
    */

    public SVDFeatureModel(SVDFeatureRawDAO dao, ObjectiveFunction objectiveFunction) {
        this.objectiveFunction = objectiveFunction;
        //extract all sizes from dao and construct index space
    }

    public ObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    private double predict(SVDFeatureInstance ins, StochasticOracle outOrc,
                          RealVector outUfactSum, RealVector outIfactSum) {
        double pred = 0.0;
        for (int i=0; i<ins.gfeas.size(); i++) {
            int ind = ins.gfeas.get(i).index;
            double val = ins.gfeas.get(i).value;
            if (outOrc != null) {
                outOrc.addScalarOracle("biases", ind, val);
            }
            pred += getScalarVarByNameIndex("bias", ind) * val;
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ufeas.size(); i++) {
            int index = ins.ufeas.get(i).index;
            outUfactSum.mapMultiplyToSelf(ins.ufeas.get(i).value);
            outUfactSum.combineToSelf(1.0, 1.0, getVectorVarByNameIndex("factors", index));
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ifeas.size(); i++) {
            int index = ins.ifeas.get(i).index;
            outIfactSum.mapMultiplyToSelf(ins.ifeas.get(i).value);
            outIfactSum.combineToSelf(1.0, 1.0, getVectorVarByNameIndex("factors", index));
        }

        pred += outUfactSum.dotProduct(outIfactSum);
        return pred;
    }

    public StochasticOracle getStochasticOracle(LearningInstance inIns) {
        SVDFeatureInstance ins;
        if (inIns instanceof SVDFeatureInstance) {
            ins = (SVDFeatureInstance) inIns;
        } else {
            return null;
        }
        StochasticOracle orc = new StochasticOracle();
        RealVector ufactSum = MatrixUtils.createRealVector(new double[factDim]);
        RealVector ifactSum = MatrixUtils.createRealVector(new double[factDim]);
        double pred = predict(ins, orc, ufactSum, ifactSum);
   
        RealVector leftGrad = ifactSum;
        RealVector rightGrad = ufactSum;
        String name = "factors";
        for (int i=0; i<ins.ufeas.size(); i++) {
            orc.addVectorOracle(name, ins.ufeas.get(i).index,
                    leftGrad.mapMultiply(ins.ufeas.get(i).value));
        }
        for (int i=0; i<ins.ifeas.size(); i++) {
            orc.addVectorOracle(name, ins.ifeas.get(i).index,
                    rightGrad.mapMultiply(ins.ifeas.get(i).value));
        }

        orc.setModelOutput(pred);
        orc.setInsLabel(ins.label);
        orc.setInsWeight(ins.weight);
        return orc;
    }

    public double predict(SVDFeatureInstance ins, boolean sigmoid) {
        RealVector ufactSum = MatrixUtils.createRealVector(new double[factDim]);
        RealVector ifactSum = MatrixUtils.createRealVector(new double[factDim]);
        double pred = predict(ins, null, ufactSum, ifactSum);
        if (sigmoid) {
            return 1 / (1 + Math.exp(-pred)); 
        } else {
            return pred;
        }
    }
}

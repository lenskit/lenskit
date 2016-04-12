package org.lenskit.mf.svdfeature;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;

import org.lenskit.featurize.Entity;
import org.lenskit.featurize.Featurizer;
import org.lenskit.solver.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel extends AbstractLearningModel implements Featurizer {
    private ObjectiveFunction objectiveFunction;
    private final Set<String> biasFeas = new HashSet<>();
    private final Set<String> ufactFeas = new HashSet<>();
    private final Set<String> ifactFeas = new HashSet<>();
    private int factDim;
    private int biasSize;
    private int factSize;


    //for training with featurized SVDFeatureInstance only.
    //  the built model is not usable because getIndex()Space is not initialized
    //  could provide a parameter for getIndex()Space
    public SVDFeatureModel(int biasSize, int factSize, int factDim,
                           ObjectiveFunction objectiveFunction) {
        super();
        this.biasSize = biasSize;
        this.factSize = factSize;
        this.factDim = factDim;
        this.objectiveFunction = objectiveFunction;
        this.variableSpace.requestScalarVar("biases", this.biasSize, 0, false, false);
        this.variableSpace.requestVectorVar("factors", this.factSize, this.factDim, 0, true, false);
    }

    public SVDFeatureModel(Set<String> biasFeas,
                           Set<String> ufactFeas,
                           Set<String> ifactFeas,
                           ObjectiveFunction objectiveFunction) {
        super();
        this.biasFeas.addAll(biasFeas);
        this.ufactFeas.addAll(ufactFeas);
        this.ifactFeas.addAll(ifactFeas);
        this.objectiveFunction = objectiveFunction;
        this.variableSpace.requestScalarVar("biases", this.biasSize, 0, false, false);
        this.variableSpace.requestVectorVar("factors", this.factSize, this.factDim, 0, true, false);
        this.indexSpace.requestStringKeyMap("biases");
        this.indexSpace.requestStringKeyMap("factors");
    }

    public LearningInstance featurize(Entity entity, boolean update) {
        //return a SVDFeatureInstance
        return null;
    }

    public ObjectiveFunction getObjectiveFunction() {
        return objectiveFunction;
    }

    private double predict(SVDFeatureInstance ins, StochasticOracle outOrc,
                          RealVector outUfactSum, RealVector outIfactSum) {
        double pred = 0.0;
        for (int i=0; i<ins.gfeas.size(); i++) {
            int ind = ins.gfeas.get(i).getIndex();
            double val = ins.gfeas.get(i).getValue();
            if (outOrc != null) {
                outOrc.addScalarOracle("biases", ind, val);
            }
            pred += getScalarVarByNameIndex("bias", ind) * val;
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ufeas.size(); i++) {
            int index = ins.ufeas.get(i).getIndex();
            outUfactSum.mapMultiplyToSelf(ins.ufeas.get(i).getValue());
            outUfactSum.combineToSelf(1.0, 1.0, getVectorVarByNameIndex("factors", index));
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ifeas.size(); i++) {
            int index = ins.ifeas.get(i).getIndex();
            outIfactSum.mapMultiplyToSelf(ins.ifeas.get(i).getValue());
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
            //raise exception
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
            orc.addVectorOracle(name, ins.ufeas.get(i).getIndex(),
                    leftGrad.mapMultiply(ins.ufeas.get(i).getValue()));
        }
        for (int i=0; i<ins.ifeas.size(); i++) {
            orc.addVectorOracle(name, ins.ifeas.get(i).getIndex(),
                    rightGrad.mapMultiply(ins.ifeas.get(i).getValue()));
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

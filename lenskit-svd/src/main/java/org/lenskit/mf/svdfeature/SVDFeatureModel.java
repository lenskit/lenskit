package org.lenskit.mf.svdfeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.MatrixUtils;

import org.apache.commons.math3.linear.RealVector;

import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.featurizer.Feature;
import org.lenskit.featurizer.FeatureExtractor;
import org.lenskit.featurizer.Featurizer;
import org.lenskit.solver.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModel extends AbstractLearningModel implements Featurizer {
    private final ObjectiveFunction objectiveFunction;
    private final Set<String> biasFeas = new HashSet<>();
    private final Set<String> ufactFeas = new HashSet<>();
    private final Set<String> ifactFeas = new HashSet<>();
    private final String labelName;
    private final String weightName;
    private final List<FeatureExtractor> featureExtractors = new ArrayList<>();
    private final int factDim;


    //for training with featurized SVDFeatureInstance only.
    //  the built model is not usable because indexSpace is not initialized
    public SVDFeatureModel(int biasSize, int factSize, int factDim,
                           ObjectiveFunction objectiveFunction) {
        super();
        this.factDim = factDim;
        this.labelName = "label";
        this.weightName = "weight";
        this.objectiveFunction = objectiveFunction;
        this.variableSpace.requestScalarVar(SVDFeatureIndexName.BIASES.get(),
                                            biasSize, 0, false, false);
        this.variableSpace.requestVectorVar(SVDFeatureIndexName.FACTORS.get(),
                                            factSize, this.factDim, 0, true, false);
    }

    public SVDFeatureModel(Set<String> biasFeas,
                           Set<String> ufactFeas,
                           Set<String> ifactFeas,
                           String labelName,
                           String weightName,
                           List<FeatureExtractor> featureExtractors,
                           int biasSize, int factSize, int factDim,
                           ObjectiveFunction objectiveFunction) {
        super();
        this.factDim = factDim;
        this.biasFeas.addAll(biasFeas);
        this.ufactFeas.addAll(ufactFeas);
        this.ifactFeas.addAll(ifactFeas);
        this.labelName = labelName;
        this.weightName = weightName;
        this.featureExtractors.addAll(featureExtractors);
        this.objectiveFunction = objectiveFunction;
        this.variableSpace.requestScalarVar(SVDFeatureIndexName.BIASES.get(),
                                            biasSize, 0, false, false);
        this.variableSpace.requestVectorVar(SVDFeatureIndexName.FACTORS.get(),
                                            factSize, this.factDim, 0, true, false);
        this.indexSpace.requestKeyMap(SVDFeatureIndexName.BIASES.get());
        this.indexSpace.requestKeyMap(SVDFeatureIndexName.FACTORS.get());
    }

    private List<Feature> getFeatures(Set<String> feaNames, Map<String, List<Feature>> feaMap) {
        List<Feature> feaList = new ArrayList<>();
        for (String feaName : feaNames) {
            if (feaMap.containsKey(feaName)) {
                feaList.addAll(feaMap.get(feaName));
            }
        }
        return feaList;
    }

    private void ensureScalarVarSpace(List<Feature> features) {
        for (Feature fea : features) {
            variableSpace.ensureScalarVar(SVDFeatureIndexName.BIASES.get(),
                                          fea.getIndex() + 1, 0, true);
        }
    }

    private void ensureVectorVarSpace(List<Feature> features) {
        for (Feature fea : features) {
            variableSpace.ensureVectorVar(SVDFeatureIndexName.FACTORS.get(),
                                          fea.getIndex() + 1, factDim,
                                          0, true, true);
        }
    }

    private String realVectorToString(RealVector vec) {
        String[] arr = new String[vec.getDimension()];
        for (int i=0; i<vec.getDimension(); i++) {
            arr[i] = Double.valueOf(vec.getEntry(i)).toString();
        }
        return StringUtils.join(arr, "\t");
    }

    public void dump(File modelFile) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(modelFile));
            RealVector biases = variableSpace.getScalarVarByName(SVDFeatureIndexName.BIASES.get());
            writer.write(Double.valueOf(biases.getEntry(0)).toString() + "\n");
            String biasLine = realVectorToString(biases.getSubVector(1, biases.getDimension() - 1));
            writer.write(biasLine + "\n");
            List<RealVector> factors = variableSpace.getVectorVarByName(SVDFeatureIndexName.FACTORS.get());
            for (int i=0; i<factors.size(); i++) {
                String factLine = realVectorToString(factors.get(i));
                writer.write(factLine + "\n");
            }
            writer.close();
        } catch (IOException e) {

        }
    }

    public LearningInstance featurize(Entity entity, boolean update) {
        Map<String, List<Feature>> feaMap = new HashMap<>();
        for (FeatureExtractor extractor : featureExtractors) {
            feaMap.putAll(extractor.extract(entity, update,
                                            indexSpace));
        }
        List<Feature> gfeas = getFeatures(biasFeas, feaMap);
        List<Feature> ufeas = getFeatures(ufactFeas, feaMap);
        List<Feature> ifeas = getFeatures(ifactFeas, feaMap);
        if (update) {
            ensureScalarVarSpace(gfeas);
            ensureVectorVarSpace(ufeas);
            ensureVectorVarSpace(ifeas);
        }
        SVDFeatureInstance ins = new SVDFeatureInstance(gfeas, ufeas, ifeas);
        if (entity.hasAttribute(labelName)) {
            ins.label = entity.getDouble(TypedName.create(labelName, Double.class));
        }
        if (entity.hasAttribute(weightName)) {
            ins.weight = entity.getDouble(TypedName.create(weightName, Double.class));
        }
        return ins;
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
                outOrc.addScalarOracle(SVDFeatureIndexName.BIASES.get(), ind, val);
            }
            pred += getScalarVarByNameIndex(SVDFeatureIndexName.BIASES.get(), ind) * val;
        }

        outUfactSum.set(0.0);
        for (int i=0; i<ins.ufeas.size(); i++) {
            int index = ins.ufeas.get(i).getIndex();
            outUfactSum.mapMultiplyToSelf(ins.ufeas.get(i).getValue());
            outUfactSum.combineToSelf(1.0, 1.0,
                                      getVectorVarByNameIndex(SVDFeatureIndexName.FACTORS.get(),
                                                              index));
        }

        outIfactSum.set(0.0);
        for (int i=0; i<ins.ifeas.size(); i++) {
            int index = ins.ifeas.get(i).getIndex();
            outIfactSum.mapMultiplyToSelf(ins.ifeas.get(i).getValue());
            outIfactSum.combineToSelf(1.0, 1.0,
                                      getVectorVarByNameIndex(SVDFeatureIndexName.FACTORS.get(),
                                                              index));
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
        for (int i=0; i<ins.ufeas.size(); i++) {
            orc.addVectorOracle(SVDFeatureIndexName.FACTORS.get(),
                                ins.ufeas.get(i).getIndex(),
                    leftGrad.mapMultiply(ins.ufeas.get(i).getValue()));
        }
        for (int i=0; i<ins.ifeas.size(); i++) {
            orc.addVectorOracle(SVDFeatureIndexName.FACTORS.get(),
                                ins.ifeas.get(i).getIndex(),
                    rightGrad.mapMultiply(ins.ifeas.get(i).getValue()));
        }

        orc.setModelOutput(pred);
        orc.setInsLabel(ins.label);
        orc.setInsWeight(ins.weight);
        return orc;
    }

    public double predict(LearningInstance ins) {
        return predict((SVDFeatureInstance)ins, true);
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

    public double predict(Entity entity, boolean sigmoid) {
        LearningInstance ins = featurize(entity, false);
        return predict((SVDFeatureInstance)ins, sigmoid);
    }
}

/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.lenskit.mf.svdfeature;

import org.apache.commons.math3.linear.MatrixUtils;

import org.apache.commons.math3.linear.RealVector;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.TypedName;
import org.lenskit.featurizer.Feature;
import org.lenskit.featurizer.FeatureExtractor;
import org.lenskit.featurizer.Featurizer;
import org.lenskit.solver.*;

import java.util.*;

/**
 * A generalized matrix factorization model: SVDFeature. See Chen et al.'s <a href="http://svdfeature.apexlab.org/wiki/Main_Page">
 * SVDFeature: A Toolkit for Feature-based Collaborative Filtering</a>. It is similar to Rendle's
 * <a href="http://www.libfm.org/">Factorization Machine</a> as well.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(SVDFeatureModelProvider.class)
public class SVDFeatureModel extends AbstractLearningModel implements Featurizer {
    private final ObjectiveFunction objectiveFunction;
    private final Set<String> biasFeas = new HashSet<>();
    private final Set<String> ufactFeas = new HashSet<>();
    private final Set<String> ifactFeas = new HashSet<>();
    private final String labelName;
    private final String weightName;
    private final List<FeatureExtractor> featureExtractors = new ArrayList<>();
    private final int factDim;

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
        ins.label = entity.getDouble(TypedName.create(labelName, Double.class));
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
        SVDFeatureInstance ins = (SVDFeatureInstance) inIns;
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

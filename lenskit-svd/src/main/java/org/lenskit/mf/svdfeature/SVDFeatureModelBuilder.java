/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.mf.svdfeature;

import java.io.IOException;

import org.grouplens.lenskit.obj.ObjectiveFunction;
import org.grouplens.lenskit.opt.OptimizationMethod;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelBuilder {
    private double tol;
    private double l1coef;
    private double l2coef;
    private double learningRate;
    private int maxIter;
    private SVDFeatureModel model;
    private ObjectiveFunction loss;
    private OptimizationMethod method;

    public SVDFeatureModelBuilder(int numGlobalFeas, int numUserfeas, int numItemFeas, int factDim,
                                  SVDFeatureInstanceDAO dao, KernelFunction outKernel, 
                                  ObjectiveFunction outLoss, OptimizationMethod outMethod, 
                                  double outL1coef, double outL2coef, int outMaxIter, 
                                  double outLearningRate, double outTol) {
        model = new SVDFeatureModel(numGlobalFeas, numUserfeas, numItemFeas, factDim, dao, outKernel);
        loss = outLoss;
        method = outMethod;
        tol = outTol;
        l1coef = outL1coef;
        l2coef = outL2coef;
        learningRate = outLearningRate;
        maxIter = outMaxIter;
    }

    public SVDFeatureModel get() throws IOException {
        method.minimize(model, loss, tol, maxIter, l1coef, l2coef, learningRate);
        return model;
    }
}

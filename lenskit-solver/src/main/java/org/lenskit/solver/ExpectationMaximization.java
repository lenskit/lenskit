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

package org.lenskit.solver;

import javax.inject.Inject;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExpectationMaximization extends AbstractOptimizationMethod {
    private int maxIter;
    private double tol;
    private OptimizationMethod method;

    @Inject
    public ExpectationMaximization() {
        maxIter = 50;
        tol = 1.0;
        method = new StochasticGradientDescent(3, 0.0, 0.01, 10);
    }

    public double minimize(LearningModel learningModel, LearningData learningData, LearningData validData) {
        //check the type of learningModel
        LatentLearningModel model = (LatentLearningModel)learningModel;
        ObjectiveTerminationCriterion termCrit = new ObjectiveTerminationCriterion(tol, maxIter);
        double objVal = 0;
        while (termCrit.keepIterate()) {
            objVal = 0;
            learningData.startNewIteration();
            LearningInstance ins;
            while ((ins = learningData.getLearningInstance()) != null) {
                objVal += model.expectation(ins);
            }
            termCrit.addIteration(objVal);
            LearningModel subModel = model.maximization();
            if (subModel != null) {
                method.minimize(subModel, learningData, validData);
            }
        }
        return objVal;
    }
}

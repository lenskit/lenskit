/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.knn;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

import java.io.Serializable;

import javax.inject.Inject;

import org.grouplens.lenskit.params.Damping;
import org.grouplens.lenskit.transform.quantize.Quantizer;
import org.grouplens.lenskit.vectors.SparseVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Similarity function that assumes the two vectors are paired samples from 2 
 * correlated random variables. Using this we estimate the mutual information
 * between the two variables.
 * 
 * Note, this uses the naive estimator of mutual information, which can be 
 * heavily biased when the two vectors have little overlap.
 * 
 * @author Daniel Kluver <kluver@cs.umn.edu>
 *
 */
public class MutualInformationVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;
    private final Quantizer quantizer;

    /**
     * @review should this take 2 quantizers?
     */
    @Inject
    public MutualInformationVectorSimilarity(Quantizer quantizer) {
        this.quantizer = quantizer;
    }

    /**
     * Note, this similarity function measures the absolute correlation between two vectors.
     * Because of this it ranges from [0,inf), not [-1,1] as specified by superclass.
     * Caution should be used when using this vector similarity function that your 
     * implementation will accept values in this range.
     * 
     * @see org.grouplens.lenskit.Similarity#similarity(java.lang.Object, java.lang.Object)
     */
    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        double n = 0.0;
        // indexed by vec1 then vec2
        double[][] jointDistribution = new double[quantizer.getCount()][quantizer.getCount()];

        // this would probably be faster if done with two pointers.
        for (Long2DoubleMap.Entry e: vec1.fast()) {
            if (!vec2.containsKey(e.getLongKey())) continue;
            double val1 = e.getDoubleValue();
            double val2 = vec2.get(e.getLongKey());
            if (Double.isNaN(val1)) continue;
            if (Double.isNaN(val2)) continue;
            
            int val1Index = quantizer.apply(val1);
            int val2Index = quantizer.apply(val2);
            jointDistribution[val1Index][val2Index]++;
            n++;
        }

        if (n == 0) {
            return 0;
        }
        
        double[] vec1Distribution = new double[quantizer.getCount()];
        double[] vec2Distribution = new double[quantizer.getCount()];

        for (int val1 = 0; val1 < quantizer.getCount(); val1++) {
            for (int val2 = 0; val2 < quantizer.getCount(); val2++) {
                // divide by n to get joint probability from our frequency counts.
                jointDistribution[val1][val2] /= n;
                vec1Distribution[val1] += jointDistribution[val1][val2];
                vec2Distribution[val2] += jointDistribution[val1][val2];
            }
        }        
        return mutualInfo(vec1Distribution, vec2Distribution, jointDistribution);
    }

    private double mutualInfo(double[] vec1Distribution, double[] vec2Distribution, double[][] jointDistribution) {
        double info = 0.0;
        for (int val1 = 0; val1 < quantizer.getCount(); val1++) {
            for (int val2 = 0; val2 < quantizer.getCount() ; val2++) {
                double joint = jointDistribution[val1][val2];
                if(joint != 0) {
                    info += joint * Math.log(joint/(vec1Distribution[val2]*vec2Distribution[val1]))/Math.log(2);
                }
            }
        }
        return info;
    }

    @Override
    public boolean isSparse() {
        return true;
    }

    @Override
    public boolean isSymmetric() {
        return true;
    }
}

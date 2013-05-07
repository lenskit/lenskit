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
package org.grouplens.lenskit.vectors.similarity;

import org.grouplens.lenskit.transform.quantize.Quantizer;
import org.grouplens.lenskit.util.statistics.MutualInformationAccumulator;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.Vectors;

import javax.inject.Inject;
import java.io.Serializable;

/**
 * Similarity function that assumes the two vectors are paired samples from 2
 * correlated random variables. Using this we estimate the mutual information
 * between the two variables.
 *
 * Note, this uses the naive estimator of mutual information, which can be
 * heavily biased when the two vectors have little overlap.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MutualInformationVectorSimilarity implements VectorSimilarity, Serializable {

    private static final long serialVersionUID = 1L;

    private final Quantizer quantizer;

    /**
     * Construct a new mutual information similarity.
     *
     * @param quantizer A quantizer to allow discrete mutual information to be computed.
     */
    @Inject
    public MutualInformationVectorSimilarity(Quantizer quantizer) {
        this.quantizer = quantizer;
    }

    /**
     * Compute similarity using mutual information.
     * <p>
     *     Note, this similarity function measures the
     * absolute correlation between two vectors. Because of this it ranges from [0,inf), not [-1,1]
     * as specified by superclass. Caution should be used when using this vector similarity function
     * that your implementation will accept values in this range.
     * </p>
     *
     * @param vec1 The first vector.
     * @param vec2 The second vector.
     * @return The similarity between the two vectors, based on mutual information.
     * @see VectorSimilarity#similarity(SparseVector, SparseVector)
     */
    @Override
    public double similarity(SparseVector vec1, SparseVector vec2) {
        MutualInformationAccumulator accum = new MutualInformationAccumulator(quantizer.getCount());

        for (Vectors.EntryPair e: Vectors.paired(vec1, vec2)) {
            accum.count(quantizer.index(e.getValue1()),
                        quantizer.index(e.getValue2()));
        }

        return accum.getMutualInformation();
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

/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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


/**
 * Significance-weighted neighborhoodFinder of Pearson correlation.
 *
 * This similarity function is like {@link PearsonCorrelation}, but it does
 * similarity weighting.  When applied to user rating vectors, if the number
 * of co-rated items <i>n</i> is less than the threshold <i>T</i>, then the
 * similarity is multiplied by <i>n/T</i>.  This decreases the importance of
 * the similarity between vectors with few ratings in common.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SignificanceWeightedPearsonCorrelation extends PearsonCorrelation {
    private final int threshold;

    public SignificanceWeightedPearsonCorrelation(int thresh) {
        threshold = thresh;
    }

    @Override
    protected double computeFinalCorrelation(int nCoratings, double dot, double var1, double var2) {
        double v = super.computeFinalCorrelation(nCoratings, dot, var1, var2);
        if (nCoratings < threshold)
            v *= (double) nCoratings / threshold;
        return v;
    }

}

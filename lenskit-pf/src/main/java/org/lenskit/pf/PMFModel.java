/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.pf;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.special.Gamma;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.util.math.Vectors;

import java.util.*;
import java.util.stream.IntStream;


public class PMFModel {

    private Int2ObjectMap<ModelEntry> rows;
    //TODO change to openhashmap
    private Int2DoubleOpenHashMap gammaOrLambdaRteSecondTerm;

    public PMFModel() {
        rows = new Int2ObjectOpenHashMap<>();
        gammaOrLambdaRteSecondTerm = new Int2DoubleOpenHashMap();
    }

    public PMFModel addEntry(ModelEntry entry) {
        int row = entry.getRowNumber();
        rows.put(row, entry);
        int featureCount = entry.getFeatureCount();
        for (int k = 0; k < featureCount; k++) {
            double value = entry.getGammaOrLambdaShpEntry(k) / entry.getGammaOrLambdaRteEntry(k);
            gammaOrLambdaRteSecondTerm.addTo(k, value);
        }
        return this;
    }

    public PMFModel addAll(PMFModel other) {
        rows.putAll(other.getRows());
        for (int k = 0; k < other.getGammaOrLambdaRteSecondTerm().size(); k++) {
            double value = other.getGammaOrLambdaRteSecondTerm().get(k);
            gammaOrLambdaRteSecondTerm.addTo(k, value);
        }
        return this;
    }

    public void initialize(double a, double aPrime, double bPrime, int userOrItemNum, int featureCount, double maxOffsetShp, double maxOffsetRte, Random random) {
        final double kRte = aPrime + featureCount;
        PMFModel model = IntStream.range(0, userOrItemNum).parallel().mapToObj(e -> {

            ModelEntry entry = new ModelEntry(e, featureCount, a, aPrime, bPrime);
            for (int k = 0; k < featureCount; k++) {
                double valueShp = a + maxOffsetShp*random.nextDouble();
                double valueRte = aPrime + maxOffsetRte*random.nextDouble();
                entry.setEntryGammaOrLambdaShp(k, valueShp);
                entry.setEntryGammaOrLambdaRte(k, valueRte);
            }

            double kShp = aPrime + maxOffsetShp*random.nextDouble();
            entry.setEntryKappaOrTauRte(kRte);
            entry.setEntryKappaOrTauShp(kShp);
            return entry;}).collect(new PMFModelCollector());
            addAll(model);
    }

    public Int2ObjectMap<ModelEntry> getRows() {
        return rows;
    }

    public Int2DoubleMap getGammaOrLambdaRteSecondTerm() {
        return gammaOrLambdaRteSecondTerm;
    }


    public double getGammaOrLambdaShpEntry(int row, int col) {
        ModelEntry entry = rows.get(row);
        if (entry == null) return 0;
        return entry.getGammaOrLambdaShpEntry(col);
    }

    public double getGammaOrLambdaRteEntry(int row, int col) {
        ModelEntry entry = rows.get(row);
        if (entry == null) return 0;
        return entry.getGammaOrLambdaRteEntry(col);
    }

    public double getKappaOrTauShpEntry(int row) {
        ModelEntry entry = rows.get(row);
        if (entry == null) return 0;
        return entry.getKappaOrTauShpEntry();
    }

    public double getKappaOrTauRteEntry(int row) {
        ModelEntry entry = rows.get(row);
        if (entry == null) return 0;
        return entry.getKappaOrTauRteEntry();
    }


    public static ModelEntry computeUserUpdate(List<RatingMatrixEntry> ratings, PMFModel preUserModel, PMFModel preItemModel, PFHyperParameters hyperParameters) {

        final int featureCount = hyperParameters.getFeatureCount();
        RealVector phiUI = MatrixUtils.createRealVector(new double[featureCount]);
        final int user = ratings.iterator().next().getUserIndex();
        final double a = hyperParameters.getA();
        final double aPrime = hyperParameters.getAPrime();
        final double bPrime = hyperParameters.getBPrime();

        ModelEntry currEntry = new ModelEntry(user, featureCount, a, aPrime, bPrime);

        Iterator<RatingMatrixEntry> userRatings = ratings.iterator();
        while (userRatings.hasNext()) {
            RatingMatrixEntry entry = userRatings.next();
            int item = entry.getItemIndex();
            double ratingUI = entry.getValue();
            if (ratingUI <= 0) {
                continue;
            }

            for (int k = 0; k < featureCount; k++) {
                double gammaShpUK = preUserModel.getGammaOrLambdaShpEntry(user, k);
                double gammaRteUK = preUserModel.getGammaOrLambdaRteEntry(user, k);
                double lambdaShpIK = preItemModel.getGammaOrLambdaShpEntry(item, k);
                double lambdaRteIK = preItemModel.getGammaOrLambdaRteEntry(item, k);
                double phiUIK = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                phiUI.setEntry(k, phiUIK);
            }
            logNormalize(phiUI);
//                double sumOfPhi = phiUI.getL1Norm();
//                logger.debug("Sum of phi vector is {}", sumOfPhi);

            if (ratingUI > 1) {
                phiUI.mapMultiplyToSelf(ratingUI);
            }

            for (int k = 0; k < featureCount; k++) {
                double value = phiUI.getEntry(k) + currEntry.getGammaOrLambdaShpEntry(k);
                currEntry.setEntryGammaOrLambdaShp(k, value);
            }

        }

        double gammaOrLambdaRteFirstTerm =  preUserModel.getKappaOrTauShpEntry(user) / preUserModel.getKappaOrTauRteEntry(user);
        for (int k = 0; k < featureCount; k++) {
            double currentGammaOrLamdaRte =  gammaOrLambdaRteFirstTerm + preItemModel.getGammaOrLambdaRteSecondTerm().get(k);
            currEntry.setEntryGammaOrLambdaRte(k, currentGammaOrLamdaRte);
            double currentKappaOrTauRte = currEntry.getKappaOrTauRteEntry() + currEntry.getGammaOrLambdaShpEntry(k) / currentGammaOrLamdaRte;
            currEntry.setEntryKappaOrTauRte(currentKappaOrTauRte);
        }

        return currEntry;
    }



    public static ModelEntry computeItemUpdate(List<RatingMatrixEntry> ratings, PMFModel preUserModel, PMFModel preItemModel, PMFModel currUserModel, PFHyperParameters hyperParameters) {

        final int featureCount = hyperParameters.getFeatureCount();
        RealVector phiUI = MatrixUtils.createRealVector(new double[featureCount]);
        final int item = ratings.iterator().next().getItemIndex();
        final double c = hyperParameters.getC();
        final double cPrime = hyperParameters.getCPrime();
        final double dPrime = hyperParameters.getDPrime();

        //TODO FIX IT IF there is no user or item ratings in training list but in validation
        //TODO ex. validation set has max all the user or item with max user number. so this will not create modelentry in model
        ModelEntry currEntry = new ModelEntry(item, featureCount, c, cPrime, dPrime);

        Iterator<RatingMatrixEntry> itemRatings = ratings.iterator();
        while (itemRatings.hasNext()) {
            RatingMatrixEntry entry = itemRatings.next();
            int user = entry.getUserIndex();
            double ratingUI = entry.getValue();
            if (ratingUI <= 0) {
                continue;
            }

            for (int k = 0; k < featureCount; k++) {
                double gammaShpUK = preUserModel.getGammaOrLambdaShpEntry(user, k);
                double gammaRteUK = preUserModel.getGammaOrLambdaRteEntry(user, k);
                double lambdaShpIK = preItemModel.getGammaOrLambdaShpEntry(item, k);
                double lambdaRteIK = preItemModel.getGammaOrLambdaRteEntry(item, k);
                double phiUIK = Gamma.digamma(gammaShpUK) - Math.log(gammaRteUK) + Gamma.digamma(lambdaShpIK) - Math.log(lambdaRteIK);
                phiUI.setEntry(k, phiUIK);
            }
            logNormalize(phiUI);
//                double sumOfPhi = phiUI.getL1Norm();
//                logger.debug("Sum of phi vector is {}", sumOfPhi);

            if (ratingUI > 1) {
                phiUI.mapMultiplyToSelf(ratingUI);
            }

            for (int k = 0; k < featureCount; k++) {
                double value = phiUI.getEntry(k) + currEntry.getGammaOrLambdaShpEntry(k);
                currEntry.setEntryGammaOrLambdaShp(k, value);
            }

        }

        double gammaOrLambdaRteFirstTerm =  preItemModel.getKappaOrTauShpEntry(item) / preItemModel.getKappaOrTauRteEntry(item);
        for (int k = 0; k < featureCount; k++) {
            double currentGammaOrLamdaRte =  gammaOrLambdaRteFirstTerm + currUserModel.getGammaOrLambdaRteSecondTerm().get(k);
            currEntry.setEntryGammaOrLambdaRte(k, currentGammaOrLamdaRte);
            double currentKappaOrTauRte = currEntry.getKappaOrTauRteEntry() + currEntry.getGammaOrLambdaShpEntry(k) / currentGammaOrLamdaRte;
            currEntry.setEntryKappaOrTauRte(currentKappaOrTauRte);
        }

        return currEntry;
    }



    public static void logNormalize (RealVector phi) {
        final int size = phi.getDimension();
        if (size == 1) {
            phi.setEntry(0, 1.0);
        }

        if (size > 1) {
            double logsum = phi.getEntry(0);
            for (int i = 1; i < size; i++) {
                double phiK = phi.getEntry(i);
                if (phiK < logsum) {
                    logsum = logsum + Math.log(1 + Math.exp(phiK - logsum));
                } else {
                    logsum = phiK + Math.log(1 + Math.exp(logsum - phiK));
                }
            }

            for (int k = 0; k < size; k++) {
                double phiK = phi.getEntry(k);
                double normalized = Math.exp(phiK - logsum);
                phi.setEntry(k, normalized);
            }
        }

    }


    public static class ModelEntry {
        private double[] gammaOrLambdaShp;
        private double[] gammaOrLambdaRte;
        private double kappaOrTauShp;
        private double kappaOrTauRte;
        private final int rowNumber;

        public ModelEntry(int rowNum, int k, double a, double aPrime, double bPrime) {
            gammaOrLambdaShp = new double[k];
            gammaOrLambdaRte = new double[k];
            kappaOrTauShp = aPrime + k * a;
            kappaOrTauRte = aPrime/bPrime;
            Arrays.fill(gammaOrLambdaShp, a);
            rowNumber = rowNum;
        }

        public void setEntryGammaOrLambdaShp(int index, double value) {
            assert index < gammaOrLambdaShp.length && index >= 0;
            gammaOrLambdaShp[index] = value;
        }

        public void setEntryGammaOrLambdaRte(int index, double value) {
            assert index < gammaOrLambdaRte.length && index >= 0;
            gammaOrLambdaRte[index] = value;
        }

        public void setEntryKappaOrTauShp(double value) {
            kappaOrTauShp = value;
        }

        public void setEntryKappaOrTauRte(double value) {
            kappaOrTauRte = value;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public int getFeatureCount() {
            return gammaOrLambdaRte.length;
        }

        public double getGammaOrLambdaShpEntry(int col) {
            return gammaOrLambdaShp[col];
        }

        public double getGammaOrLambdaRteEntry(int col) {
            return gammaOrLambdaRte[col];
        }

        public double[] getGammaOrLambdaShp() {
            return gammaOrLambdaShp;
        }

        public double[] getGammaOrLambdaRte() {
            return gammaOrLambdaRte;
        }

        public double getKappaOrTauShpEntry() {
            return kappaOrTauShp;
        }

        public double getKappaOrTauRteEntry() {
            return kappaOrTauRte;
        }

    }
}

/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.pf;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.data.ratings.RatingMatrixEntry;
import org.lenskit.util.math.Scalars;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;


class PMFModel {

    private Int2ObjectMap<ModelEntry> model;
    private Int2DoubleOpenHashMap sumOfMeanWeight;

    public PMFModel() {
        model = new Int2ObjectOpenHashMap<>();
        sumOfMeanWeight = new Int2DoubleOpenHashMap();
    }

    public PMFModel addEntry(ModelEntry entry) {
        int index = entry.getIndex();
        model.put(index, entry);
        int featureCount = entry.getFeatureCount();
        for (int k = 0; k < featureCount; k++) {
            double value = entry.getWeightShpEntry(k) / entry.getWeightRteEntry(k);
            sumOfMeanWeight.addTo(k, value);
        }
        return this;
    }

    public PMFModel addAll(PMFModel other) {
        model.putAll(other.getModel());
        for (int k = 0; k < other.getSumOfMeanWeight().size(); k++) {
            double value = other.getSumOfMeanWeight().get(k);
            sumOfMeanWeight.addTo(k, value);
        }
        return this;
    }

    public void initialize(double weightShpPrior, double activityShpPrior, double meanActivityPrior, int userOrItemNum,
                           int featureCount, double maxOffsetShp, double maxOffsetRte, Random random) {
        final double activityRte = activityShpPrior + featureCount;
        PMFModel model = IntStream.range(0, userOrItemNum)
                .parallel()
                .mapToObj(e -> {
                    ModelEntry entry = new ModelEntry(e, featureCount, weightShpPrior, activityShpPrior, meanActivityPrior);
                    for (int k = 0; k < featureCount; k++) {
                        double valueShp = weightShpPrior + maxOffsetShp*random.nextDouble();
                        double valueRte = activityShpPrior + maxOffsetRte*random.nextDouble();
                        entry.setWeightShpEntry(k, valueShp);
                        entry.setWeightRteEntry(k, valueRte);
                    }
                    double activityShp = activityShpPrior + maxOffsetShp*random.nextDouble();
                    entry.setActivityRte(activityRte);
                    entry.setActivityShp(activityShp);
                    return entry;
                })
                .collect(new PMFModelCollector());
        addAll(model);
    }

    public Int2ObjectMap<ModelEntry> getModel() {
        return model;
    }

    public Int2DoubleMap getSumOfMeanWeight() {
        return sumOfMeanWeight;
    }


    public double getWeightShpEntry(int index, int featureIndex) {
        ModelEntry entry = model.get(index);
        if (entry == null) return 0;
        return entry.getWeightShpEntry(featureIndex);
    }

    public double getWeightRteEntry(int index, int featureIndex) {
        ModelEntry entry = model.get(index);
        if (entry == null) return 0;
        return entry.getWeightRteEntry(featureIndex);
    }

    public double getActivityShp(int index) {
        ModelEntry entry = model.get(index);
        if (entry == null) return 0;
        return entry.getActivityShp();
    }

    public double getActivityRte(int index) {
        ModelEntry entry = model.get(index);
        if (entry == null) return 0;
        return entry.getActivityRte();
    }


    public static ModelEntry computeUserUpdate(List<RatingMatrixEntry> ratings, PMFModel preUserModel, PMFModel preItemModel, PFHyperParameters hyperParameters) {

        final int featureCount = hyperParameters.getFeatureCount();
        RealVector phiUI = MatrixUtils.createRealVector(new double[featureCount]);
        final int user = ratings.get(0).getUserIndex();
        final double userWeightShpPrior = hyperParameters.getUserWeightShpPrior();
        final double userActivityShpPrior = hyperParameters.getUserActivityShpPrior();
        final double userActivityPriorMean = hyperParameters.getUserActivityPriorMean();

        ModelEntry modelEntry = new ModelEntry(user, featureCount, userWeightShpPrior, userActivityShpPrior, userActivityPriorMean);

        Iterator<RatingMatrixEntry> userRatings = ratings.iterator();
        while (userRatings.hasNext()) {
            RatingMatrixEntry entry = userRatings.next();
            int item = entry.getItemIndex();
            double rating = entry.getValue();
            if (rating <= 0) {
                continue;
            }

            updatePhi(phiUI, user, item, rating, featureCount, preUserModel, preItemModel);

            for (int k = 0; k < featureCount; k++) {
                double value = phiUI.getEntry(k) + modelEntry.getWeightShpEntry(k);
                modelEntry.setWeightShpEntry(k, value);
            }

        }

        // update user weight rate and user activity rate
        double meanUserActivity =  preUserModel.getActivityShp(user) / preUserModel.getActivityRte(user);
        for (int k = 0; k < featureCount; k++) {
            double userWeightRteK =  meanUserActivity + preItemModel.getSumOfMeanWeight().get(k);
            modelEntry.setWeightRteEntry(k, userWeightRteK);
            double userActivityRte = modelEntry.getActivityRte() + modelEntry.getWeightShpEntry(k) / userWeightRteK;
            modelEntry.setActivityRte(userActivityRte);
        }

        return modelEntry;
    }



    public static ModelEntry computeItemUpdate(List<RatingMatrixEntry> ratings, PMFModel preUserModel, PMFModel preItemModel, PMFModel currUserModel, PFHyperParameters hyperParameters) {

        final int featureCount = hyperParameters.getFeatureCount();
        RealVector phiUI = MatrixUtils.createRealVector(new double[featureCount]);
        final int item = ratings.get(0).getItemIndex();
        final double itemWeightShpPrior = hyperParameters.getItemWeightShpPrior();
        final double itemActivityShpPrior = hyperParameters.getItemActivityShpPrior();
        final double itemActivityPriorMean = hyperParameters.getItemActivityPriorMean();

        ModelEntry modelEntry = new ModelEntry(item, featureCount, itemWeightShpPrior, itemActivityShpPrior, itemActivityPriorMean);

        Iterator<RatingMatrixEntry> itemRatings = ratings.iterator();
        while (itemRatings.hasNext()) {
            RatingMatrixEntry entry = itemRatings.next();
            int user = entry.getUserIndex();
            double rating = entry.getValue();
            if (rating <= 0) {
                continue;
            }

            updatePhi(phiUI, user, item, rating, featureCount, preUserModel, preItemModel);

            for (int k = 0; k < featureCount; k++) {
                double value = phiUI.getEntry(k) + modelEntry.getWeightShpEntry(k);
                modelEntry.setWeightShpEntry(k, value);
            }

        }

        // update item weight rate and item activity rate
        double meanItemActivity =  preItemModel.getActivityShp(item) / preItemModel.getActivityRte(item);
        for (int k = 0; k < featureCount; k++) {
            double itemWeightRteK =  meanItemActivity + currUserModel.getSumOfMeanWeight().get(k);
            modelEntry.setWeightRteEntry(k, itemWeightRteK);
            double itemActivityRte = modelEntry.getActivityRte() + modelEntry.getWeightShpEntry(k) / itemWeightRteK;
            modelEntry.setActivityRte(itemActivityRte);
        }

        return modelEntry;
    }

    private static void updatePhi(RealVector phi, int user, int item, double rating,
                                  int featureCount, PMFModel userModel, PMFModel itemModel) {
        for (int k = 0; k < featureCount; k++) {
            double userWeightShp = userModel.getWeightShpEntry(user, k);
            double userWeightRte = userModel.getWeightRteEntry(user, k);
            double itemWeightShp = itemModel.getWeightShpEntry(item, k);
            double itemWeightRte = itemModel.getWeightRteEntry(item, k);
            double phiUIK = Scalars.digamma(userWeightShp) - Math.log(userWeightRte) + Scalars.digamma(itemWeightShp) - Math.log(itemWeightRte);
            phi.setEntry(k, phiUIK);
        }
        logNormalize(phi);

        if (rating > 1) {
            phi.mapMultiplyToSelf(rating);
        }
    }


    private static void logNormalize (RealVector phi) {
        final int size = phi.getDimension();
        if (size == 1) {
            phi.setEntry(0, 1.0);
        }

        if (size > 1) {
            double logsum = phi.getEntry(0);
            for (int k = 1; k < size; k++) {
                double phiK = phi.getEntry(k);
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
        private double[] weightShp;
        private double[] weightRte;
        private double activityShp;
        private double activityRte;
        private final int index;

        public ModelEntry(int userOrItemIndex, int featureCount, double weightShpPrior, double activityShpPrior, double activityPriorMean) {
            weightShp = new double[featureCount];
            weightRte = new double[featureCount];
            activityShp = activityShpPrior + featureCount * weightShpPrior;
            activityRte = activityShpPrior/activityPriorMean;
            Arrays.fill(weightShp, weightShpPrior);
            index = userOrItemIndex;
        }

        public void setWeightShpEntry(int index, double value) {
            assert index < weightShp.length && index >= 0;
            weightShp[index] = value;
        }

        public void setWeightRteEntry(int index, double value) {
            assert index < weightRte.length && index >= 0;
            weightRte[index] = value;
        }

        public void setActivityShp(double value) {
            activityShp = value;
        }

        public void setActivityRte(double value) {
            activityRte = value;
        }

        public int getIndex() {
            return index;
        }

        public int getFeatureCount() {
            return weightRte.length;
        }

        public double getWeightShpEntry(int col) {
            return weightShp[col];
        }

        public double getWeightRteEntry(int col) {
            return weightRte[col];
        }

        public double[] getWeightShp() {
            return weightShp;
        }

        public double[] getWeightRte() {
            return weightRte;
        }

        public double getActivityShp() {
            return activityShp;
        }

        public double getActivityRte() {
            return activityRte;
        }

    }
}

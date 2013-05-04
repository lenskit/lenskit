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
package org.grouplens.lenskit.mf.funksvd;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.Index;

import java.io.Serializable;
import java.util.List;

/**
 * The FunkSVD model class.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(FunkSVDModelBuilder.class)
@Shareable
public class FunkSVDModel implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Number of features in the vector.
     */
    public final int featureCount;
    /**
     * The number of users.
     */
    public final int numUser;
    /**
     * The number of items.
     */
    public final int numItem;

    /**
     * The item-feature matrix (features, then items).  Do not modify this array.
     */
    public final double[][] itemFeatures;
    /**
     * The user-feature matrix (features, then users).  Do not modify this array.
     */
    public final double[][] userFeatures;

    /**
     * The clamping function used to build this model.
     */
    public final ClampingFunction clampingFunction;

    /**
     * The feature info for each feature.
     */
    public final List<FeatureInfo> featureInfo;

    /**
     * The final RMSE of
     */

    /**
     * The item index.
     */
    public final Index itemIndex;
    /**
     * The user index.
     */
    public final Index userIndex;

    /**
     * The baseline predictor used to build this model.
     */
    public final BaselinePredictor baseline;

    public FunkSVDModel(int nfeatures, double[][] ifeats, double[][] ufeats,
                        ClampingFunction clamp, Index iidx, Index uidx,
                        BaselinePredictor baseline, List<FeatureInfo> features) {
        featureCount = nfeatures;
        clampingFunction = clamp;
        this.baseline = baseline;

        itemFeatures = ifeats;
        userFeatures = ufeats;

        itemIndex = iidx;
        userIndex = uidx;

        numItem = iidx.getIds().size();
        numUser = uidx.getIds().size();

        featureInfo = ImmutableList.copyOf(features);
    }

    /**
     * Get the model's feature count.
     *
     * @return The model's feature count.
     */
    public int getFeatureCount() {
        return featureCount;
    }

    /**
     * Get the {@link FeatureInfo} for a particular feature.
     * @param f The feature number.
     * @return The feature's summary information.
     */
    public FeatureInfo getFeatureInfo(int f) {
        return featureInfo.get(f);
    }

    /**
     * Get the metadata about all features.
     * @return The feature metadata.
     */
    public List<FeatureInfo> getFeatureInfo() {
        return featureInfo;
    }
}

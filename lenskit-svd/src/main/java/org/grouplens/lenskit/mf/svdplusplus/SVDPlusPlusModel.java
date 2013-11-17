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
package org.grouplens.lenskit.mf.svdplusplus;

import com.google.common.collect.ImmutableList;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.grouplens.lenskit.mf.funksvd.FeatureInfo;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.Index;

import java.io.Serializable;
import java.util.List;
import java.lang.Math;

/**
 * The SVDPlusPlus model class.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(SVDPlusPlusModelBuilder.class)
@Shareable
public final class SVDPlusPlusModel implements Serializable {
    private static final long serialVersionUID = 2L;

    private final int featureCount;

    private final int numUser;
    private final int numItem;

    private final double[][] itemFeatures;
    private final double[][] itemImpFeatures;
    private final double[][] userFeatures;

    private final ClampingFunction clampingFunction;

    private final List<FeatureInfo> featureInfo;

    private final Index itemIndex;
    private final Index userIndex;

    public SVDPlusPlusModel(int nfeatures, double[][] ifeats, double[][] ufeats, double[][] iimpfats,
                        ClampingFunction clamp, Index iidx, Index uidx, List<FeatureInfo> features) {
        featureCount = nfeatures;
        clampingFunction = clamp;

        itemFeatures = ifeats;
        itemImpFeatures = iimpfats;
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

    /**
     * The number of users.
     */
    public int getUserCount() {
        return numUser;
    }

    /**
     * The number of items.
     */
    public int getItemCount() {
        return numItem;
    }

    /**
     * The item-feature matrix (features, then items).  Do not modify this array.
     */
    @Deprecated
    public double[][] getItemFeatures() {
        return itemFeatures;
    }

    /**
     * The item-feature matrix (features, then items).  Do not modify this array.
     */
    @Deprecated
    public double[][] getItemImpFeatures() {
        return itemImpFeatures;
    }

    /**
     * The user-feature matrix (features, then users).  Do not modify this array.
     */
    @Deprecated
    public double[][] getUserFeatures() {
        return userFeatures;
    }

    /**
     * The clamping function used to build this model.
     */
    public ClampingFunction getClampingFunction() {
        return clampingFunction;
    }

    /**
     * The item index.
     */
    public Index getItemIndex() {
        return itemIndex;
    }

    /**
     * The user index.
     */
    public Index getUserIndex() {
        return userIndex;
    }

    /**
     * Get a particular feature value for an item.
     * @param iid The item ID.
     * @param feature The feature.
     * @return The item-feature value, or 0 if the item was not in the training set.
     */
    public double getItemFeature(long iid, int feature) {
        int iidx = itemIndex.getIndex(iid);
        if (iidx < 0) {
            return 0;
        } else {
            return itemFeatures[feature][iidx];
        }
    }

    /**
     * Get a particular implicit feature value for an item.
     * @param iid The item ID.
     * @param feature The feature.
     * @return The item-implicit-feature value, or 0 if the item was not in the training set.
     */
    public double getItemImpFeature(long iid, int feature) {
        int iidx = itemIndex.getIndex(iid);
        if (iidx < 0) {
            return 0;
        } else {
            return itemImpFeatures[feature][iidx];
        }
    }


    /**
     * Get a particular feature value for an user.
     * @param uid The item ID.
     * @param feature The feature.
     * @return The user-feature value, or 0 if the user was not in the training set.
     */
    public double getUserFeature(long uid, int feature) {
        int uidx = userIndex.getIndex(uid);
        if (uidx < 0) {
            return 0;
        } else {
            return userFeatures[feature][uidx];
        }
    }
}

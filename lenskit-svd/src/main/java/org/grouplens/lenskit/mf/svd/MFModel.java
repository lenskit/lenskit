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
package org.grouplens.lenskit.mf.svd;

import org.grouplens.lenskit.indexes.IdIndexMapping;

import java.io.Serializable;

/**
 * Common model for matrix factorization (SVD) recommendation.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MFModel implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int featureCount;
    protected final int numUser;
    protected final int numItem;
    protected final double[][] itemFeatures;
    protected final double[][] userFeatures;
    protected final IdIndexMapping itemIndex;
    protected final IdIndexMapping userIndex;

    public MFModel(int nfeatures, double[][] ifeats, double[][] ufeats,
                   IdIndexMapping iidx, IdIndexMapping uidx) {
        featureCount = nfeatures;
        numUser = uidx.size();
        numItem = iidx.size();
        itemFeatures = ifeats;
        userFeatures = ufeats;
        itemIndex = iidx;
        userIndex = uidx;
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
     * The item index mapping.
     */
    public IdIndexMapping getItemIndex() {
        return itemIndex;
    }

    /**
     * The user index mapping.
     */
    public IdIndexMapping getUserIndex() {
        return userIndex;
    }

    /**
     * The item-feature matrix (features, then items).  Do not modify this array.
     */
    @Deprecated
    public double[][] getItemFeatures() {
        return itemFeatures;
    }

    /**
     * The user-feature matrix (features, then users).  Do not modify this array.
     */
    @Deprecated
    public double[][] getUserFeatures() {
        return userFeatures;
    }

    /**
     * Get a particular feature value for an item.
     * @param iid The item ID.
     * @param feature The feature.
     * @return The item-feature value, or 0 if the item was not in the training set.
     */
    public double getItemFeature(long iid, int feature) {
        int iidx = itemIndex.tryGetIndex(iid);
        if (iidx < 0) {
            return 0;
        } else {
            return itemFeatures[feature][iidx];
        }
    }

    /**
     * Get a particular feature value for an user.
     * @param uid The item ID.
     * @param feature The feature.
     * @return The user-feature value, or 0 if the user was not in the training set.
     */
    public double getUserFeature(long uid, int feature) {
        int uidx = userIndex.tryGetIndex(uid);
        if (uidx < 0) {
            return 0;
        } else {
            return userFeatures[feature][uidx];
        }
    }
}

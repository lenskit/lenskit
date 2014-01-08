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

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.indexes.IdIndexMapping;
import org.grouplens.lenskit.matrixes.ImmutableMatrix;
import org.grouplens.lenskit.matrixes.Matrix;
import org.grouplens.lenskit.vectors.Vec;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Common model for matrix factorization (SVD) recommendation.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MFModel implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int featureCount;
    protected final int userCount;
    protected final int itemCount;

    protected final ImmutableMatrix userMatrix;
    protected final ImmutableMatrix itemMatrix;
    protected final IdIndexMapping userIndex;
    protected final IdIndexMapping itemIndex;

    /**
     * Construct a matrix factorization model.
     * @param umat The user feature matrix (users x features).
     * @param imat The item feature matrix (items x features).
     * @param uidx The user index mapping.
     * @param iidx The item index mapping.
     */
    public MFModel(Matrix umat, Matrix imat,
                   IdIndexMapping uidx, IdIndexMapping iidx) {
        Preconditions.checkArgument(umat.getColumnCount() == imat.getColumnCount(),
                                    "mismatched matrix sizes");
        featureCount = umat.getColumnCount();
        userCount = uidx.size();
        itemCount = iidx.size();
        Preconditions.checkArgument(umat.getRowCount() == userCount,
                                    "user matrix has %s rows, expected %s",
                                    umat.getRowCount(), userCount);
        Preconditions.checkArgument(imat.getRowCount() == itemCount,
                                    "item matrix has %s rows, expected %s",
                                    imat.getRowCount(), itemCount);
        userMatrix = umat.immutable();
        itemMatrix = imat.immutable();
        userIndex = uidx;
        itemIndex = iidx;
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        if (userMatrix.getColumnCount() != featureCount) {
            throw new InvalidObjectException("user matrix has wrong column count");
        }
        if (userIndex.size() != userMatrix.getRowCount()) {
            throw new InvalidObjectException("user matrix and index have different row counts");
        }
        if (itemMatrix.getColumnCount() != featureCount) {
            throw new InvalidObjectException("item matrix has wrong column count");
        }
        if (itemIndex.size() != itemMatrix.getRowCount()) {
            throw new InvalidObjectException("item matrix and index have different row counts");
        }
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
        return userCount;
    }

    /**
     * The number of items.
     */
    public int getItemCount() {
        return itemCount;
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
     * Get the user matrix.
     * @return The user matrix (users x features).
     */
    public ImmutableMatrix getUserMatrix() {
        return userMatrix;
    }

    /**
     * Get the item matrix.
     * @return The item matrix (items x features).
     */
    public ImmutableMatrix getItemMatrix() {
        return itemMatrix;
    }

    @Nullable
    public Vec getUserVector(long user) {
        int uidx = userIndex.tryGetIndex(user);
        if (uidx < 0) {
            return null;
        } else {
            return userMatrix.row(uidx);
        }
    }

    @Nullable
    public Vec getItemVector(long item) {
        int iidx = itemIndex.tryGetIndex(item);
        if (iidx < 0) {
            return null;
        } else {
            return itemMatrix.row(iidx);
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
            return userMatrix.get(uidx, feature);
        }
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
            return itemMatrix.get(iidx, feature);
        }
    }
}

/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import mikera.matrixx.IMatrix;
import mikera.matrixx.Matrix;
import mikera.matrixx.impl.ImmutableMatrix;
import mikera.vectorz.AVector;
import org.grouplens.lenskit.indexes.IdIndexMapping;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Common model for matrix factorization (SVD) recommendation.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MFModel implements Serializable {
    private static final long serialVersionUID = 2L;

    // FIXME Make these final again
    protected int featureCount;
    protected int userCount;
    protected int itemCount;

    protected ImmutableMatrix userMatrix;
    protected ImmutableMatrix itemMatrix;
    protected IdIndexMapping userIndex;
    protected IdIndexMapping itemIndex;

    /**
     * Construct a matrix factorization model.  The matrices are not copied, so the caller should
     * make sure they won't be modified by anyone else.
     *
     * @param umat The user feature matrix (users x features).
     * @param imat The item feature matrix (items x features).
     * @param uidx The user index mapping.
     * @param iidx The item index mapping.
     */
    public MFModel(ImmutableMatrix umat, ImmutableMatrix imat,
                   IdIndexMapping uidx, IdIndexMapping iidx) {
        Preconditions.checkArgument(umat.columnCount() == imat.columnCount(),
                                    "mismatched matrix sizes");
        featureCount = umat.columnCount();
        userCount = uidx.size();
        itemCount = iidx.size();
        Preconditions.checkArgument(umat.rowCount() == userCount,
                                    "user matrix has %s rows, expected %s",
                                    umat.rowCount(), userCount);
        Preconditions.checkArgument(imat.rowCount() == itemCount,
                                    "item matrix has %s rows, expected %s",
                                    imat.rowCount(), itemCount);
        userMatrix = umat;
        itemMatrix = imat;
        userIndex = uidx;
        itemIndex = iidx;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(featureCount);
        out.writeInt(userCount);
        out.writeInt(itemCount);

        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                out.writeDouble(userMatrix.get(i, j));
            }
        }

        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                out.writeDouble(itemMatrix.get(i, j));
            }
        }

        out.writeObject(userIndex);
        out.writeObject(itemIndex);
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        featureCount = input.readInt();
        userCount = input.readInt();
        itemCount = input.readInt();

        Matrix umat = Matrix.create(userCount, featureCount);
        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                umat.set(i, j, input.readDouble());
            }
        }
        userMatrix = ImmutableMatrix.wrap(umat);

        Matrix imat = Matrix.create(itemCount, featureCount);
        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                imat.set(i, j, input.readDouble());
            }
        }
        itemMatrix = ImmutableMatrix.wrap(imat);

        userIndex = (IdIndexMapping) input.readObject();
        itemIndex = (IdIndexMapping) input.readObject();

        if (userIndex.size() != userMatrix.rowCount()) {
            throw new InvalidObjectException("user matrix and index have different row counts");
        }
        if (itemIndex.size() != itemMatrix.rowCount()) {
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
    public IMatrix getUserMatrix() {
        return userMatrix;
    }

    /**
     * Get the item matrix.
     * @return The item matrix (items x features).
     */
    public IMatrix getItemMatrix() {
        return itemMatrix;
    }

    @Nullable
    public AVector getUserVector(long user) {
        int uidx = userIndex.tryGetIndex(user);
        if (uidx < 0) {
            return null;
        } else {
            return userMatrix.getRow(uidx);
        }
    }

    @Nullable
    public AVector getItemVector(long item) {
        int iidx = itemIndex.tryGetIndex(item);
        if (iidx < 0) {
            return null;
        } else {
            return itemMatrix.getRow(iidx);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName())
          .append("(nu=")
          .append(getUserCount())
          .append(", ni=")
          .append(getItemCount())
          .append(", nf=")
          .append(getFeatureCount())
          .append(")");
        return sb.toString();
    }
}

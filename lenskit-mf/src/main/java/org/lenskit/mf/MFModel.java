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
package org.lenskit.mf;

import com.google.common.base.Preconditions;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.inject.Shareable;
import org.lenskit.util.keys.KeyIndex;
import org.lenskit.util.math.Vectors;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Common model for matrix factorization (SVD) recommendation.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Shareable
public class MFModel implements Serializable {
    private static final long serialVersionUID = 2L;

    // FIXME Make these final again
    protected int featureCount;
    protected int userCount;
    protected int itemCount;

    protected RealMatrix userMatrix;
    protected RealMatrix itemMatrix;
    protected KeyIndex userIndex;
    protected KeyIndex itemIndex;

    /**
     * Construct a matrix factorization model.  The matrices are not copied, so the caller should
     * make sure they won't be modified by anyone else.
     *
     * @param umat The user feature matrix (users x features).
     * @param imat The item feature matrix (items x features).
     * @param uidx The user index mapping.
     * @param iidx The item index mapping.
     */
    public MFModel(RealMatrix umat, RealMatrix imat,
                   KeyIndex uidx, KeyIndex iidx) {
        Preconditions.checkArgument(umat.getColumnDimension() == imat.getColumnDimension(),
                                    "mismatched matrix sizes");
        featureCount = umat.getColumnDimension();
        userCount = uidx.size();
        itemCount = iidx.size();
        Preconditions.checkArgument(umat.getRowDimension() == userCount,
                                    "user matrix has %s rows, expected %s",
                                    umat.getRowDimension(), userCount);
        Preconditions.checkArgument(imat.getRowDimension() == itemCount,
                                    "item matrix has %s rows, expected %s",
                                    imat.getRowDimension(), itemCount);
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
                out.writeDouble(userMatrix.getEntry(i, j));
            }
        }

        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                out.writeDouble(itemMatrix.getEntry(i, j));
            }
        }

        out.writeObject(userIndex);
        out.writeObject(itemIndex);
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        featureCount = input.readInt();
        userCount = input.readInt();
        itemCount = input.readInt();

        RealMatrix umat = MatrixUtils.createRealMatrix(userCount, featureCount);
        for (int i = 0; i < userCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                umat.setEntry(i, j, input.readDouble());
            }
        }
        userMatrix = umat;

        RealMatrix imat = MatrixUtils.createRealMatrix(itemCount, featureCount);
        for (int i = 0; i < itemCount; i++) {
            for (int j = 0; j < featureCount; j++) {
                imat.setEntry(i, j, input.readDouble());
            }
        }
        itemMatrix = imat;

        userIndex = (KeyIndex) input.readObject();
        itemIndex = (KeyIndex) input.readObject();

        if (userIndex.size() != userMatrix.getRowDimension()) {
            throw new InvalidObjectException("user matrix and index have different row counts");
        }
        if (itemIndex.size() != itemMatrix.getRowDimension()) {
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
    public KeyIndex getItemIndex() {
        return itemIndex;
    }

    /**
     * The user index mapping.
     */
    public KeyIndex getUserIndex() {
        return userIndex;
    }

    /**
     * Get the user matrix.
     * @return The user matrix (users x features).
     */
    public RealMatrix getUserMatrix() {
        return userMatrix;
    }

    /**
     * Get the item matrix.
     * @return The item matrix (items x features).
     */
    public RealMatrix getItemMatrix() {
        return itemMatrix;
    }

    @Nullable
    public RealVector getUserVector(long user) {
        int uidx = userIndex.tryGetIndex(user);
        if (uidx < 0) {
            return null;
        } else {
            return Vectors.matrixRow(userMatrix, uidx);
        }
    }

    @Nullable
    public RealVector getItemVector(long item) {
        int iidx = itemIndex.tryGetIndex(item);
        if (iidx < 0) {
            return null;
        } else {
            return Vectors.matrixRow(itemMatrix, iidx);
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
            return userMatrix.getEntry(uidx, feature);
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
            return itemMatrix.getEntry(iidx, feature);
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

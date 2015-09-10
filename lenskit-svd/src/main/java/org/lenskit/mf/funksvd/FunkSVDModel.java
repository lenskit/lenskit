/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.mf.funksvd;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.core.Shareable;
import org.lenskit.mf.svd.MFModel;
import org.lenskit.util.keys.KeyIndex;

import java.util.List;

/**
 * Model for FunkSVD recommendation.  This extends the SVD model with clamping functions and
 * information about the training of the features.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(FunkSVDModelBuilder.class)
@Shareable
public final class FunkSVDModel extends MFModel {
    private static final long serialVersionUID = 3L;

    private final List<FeatureInfo> featureInfo;
    private final RealVector averageUser;

    public FunkSVDModel(RealMatrix umat, RealMatrix imat,
                        KeyIndex uidx, KeyIndex iidx,
                        List<FeatureInfo> features) {
        super(umat, imat, uidx, iidx);

        featureInfo = ImmutableList.copyOf(features);

        double[] means = new double[featureCount];
        for (int f = featureCount - 1; f >= 0; f--) {
            means[f] = featureInfo.get(f).getUserAverage();
        }
        averageUser = MatrixUtils.createRealVector(means);
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

    public RealVector getAverageUserVector() {
        return averageUser;
    }
}

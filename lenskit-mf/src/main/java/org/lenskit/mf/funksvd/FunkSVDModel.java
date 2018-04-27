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
package org.lenskit.mf.funksvd;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.mf.MFModel;
import org.lenskit.util.keys.KeyIndex;

import java.util.List;

/**
 * Model for FunkSVD recommendation.  This extends the SVD model with clamping functions and
 * information about the training of the features.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@DefaultProvider(FunkSVDModelProvider.class)
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

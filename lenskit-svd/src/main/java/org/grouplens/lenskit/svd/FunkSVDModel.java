/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.svd;

import java.io.Serializable;

import org.grouplens.grapht.annotation.DefaultProvider;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.util.Index;

@DefaultProvider(FunkSVDModelProvider.class)
public class FunkSVDModel implements Serializable {
    private static final long serialVersionUID = -5797099617512506185L;

    public final int featureCount;
    public final double itemFeatures[][];
    public final double userFeatures[][];
    public final ClampingFunction clampingFunction;

    public final Index itemIndex;
    public final Index userIndex;
    public final BaselinePredictor baseline;

    public FunkSVDModel(int nfeatures, double[][] ifeats, double[][] ufeats,
                        ClampingFunction clamp, Index iidx, Index uidx,
                        BaselinePredictor baseline) {
        featureCount = nfeatures;
        itemFeatures = ifeats;
        userFeatures = ufeats;
        clampingFunction = clamp;
        itemIndex = iidx;
        userIndex = uidx;
        this.baseline = baseline;
    }

    public double getItemFeatureValue(int item, int feature) {
        return itemFeatures[feature][item];
    }

    public int getItemIndex(long item) {
        return itemIndex.getIndex(item);
    }
}

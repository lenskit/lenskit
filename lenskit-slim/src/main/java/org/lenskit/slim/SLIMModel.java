/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.slim;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import java.io.Serializable;

/**
 * SLIM model
 * implement paper SLIM: Sparse Linear Methods for Top-N Recommender Systems
 */
@DefaultProvider(SLIMModelProvider.class)
@Shareable
public class SLIMModel implements Serializable {
    private static final long serialVersionUID = 3L;

    private final Long2ObjectMap<Long2DoubleMap> trainedWeights;

    public SLIMModel(Long2ObjectMap<Long2DoubleMap> weights) {
        trainedWeights = weights;
    }

    @Nonnull
    public Long2DoubleMap getWeights(long item) {
        Long2DoubleMap weights = trainedWeights.get(item);
        if (weights == null) {
            weights = Long2DoubleMaps.EMPTY_MAP;
        }
        return LongUtils.frozenMap(weights);
    }
}

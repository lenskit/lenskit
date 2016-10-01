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
package org.lenskit.bias;

import org.lenskit.data.ratings.RatingSummary;
import org.lenskit.inject.Transient;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;
import org.lenskit.util.keys.SortedKeyIndex;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model with the global average rating.
 */
public class ItemAverageRatingBiasModelProvider implements Provider<ItemBiasModel> {
    private final RatingSummary summary;

    @Inject
    public ItemAverageRatingBiasModelProvider(@Transient RatingSummary rs) {
        summary = rs;
    }

    @Override
    public ItemBiasModel get() {
        SortedKeyIndex keys = SortedKeyIndex.fromCollection(summary.getItems());
        final int n = keys.size();
        double[] values = new double[n];
        for (int i = 0; i < n; i++) {
            values[i] = summary.getItemOffset(keys.getKey(i));
        }

        return new ItemBiasModel(summary.getGlobalMean(), Long2DoubleSortedArrayMap.wrap(keys, values));
    }
}

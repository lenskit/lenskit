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
package org.lenskit.bias;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import org.grouplens.grapht.annotation.DefaultProvider;
import org.lenskit.inject.Shareable;

import net.jcip.annotations.Immutable;

/**
 * Bias model that provides global and item biases. The item biases are precomputed and are *not* updated based on
 * new data added since the model builds
 */
@Shareable
@Immutable
@DefaultProvider(ItemAverageRatingBiasModelProvider.class)
public class ItemBiasModel extends UserItemBiasModel {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a new item bias model.
     * @param global The global bias.
     * @param items The item biases.
     */
    public ItemBiasModel(double global, Long2DoubleMap items) {
        super(global, Long2DoubleMaps.EMPTY_MAP, items);
    }
}

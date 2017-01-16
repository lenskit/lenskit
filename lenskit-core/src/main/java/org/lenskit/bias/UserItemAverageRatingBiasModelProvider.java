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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.data.ratings.RatingVectorPDAO;
import org.lenskit.inject.Transient;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model with users' average ratings.
 */
public class UserItemAverageRatingBiasModelProvider implements Provider<UserItemBiasModel> {
    private final ItemBiasModel itemBiases;
    private final RatingVectorPDAO dao;
    private final double damping;


    @Inject
    public UserItemAverageRatingBiasModelProvider(ItemBiasModel ib, @Transient RatingVectorPDAO dao, @BiasDamping double damp) {
        itemBiases = ib;
        this.dao = dao;
        damping = damp;
    }

    @Override
    public UserItemBiasModel get() {
        double intercept = itemBiases.getIntercept();
        Long2DoubleMap itemOff = itemBiases.getItemBiases();

        Long2DoubleMap map = new Long2DoubleOpenHashMap();
        try (ObjectStream<IdBox<Long2DoubleMap>> stream = dao.streamUsers()) {
            for (IdBox<Long2DoubleMap> user : stream) {
                Long2DoubleMap uvec = user.getValue();

                double usum = 0;

                for (Long2DoubleMap.Entry e: uvec.long2DoubleEntrySet()) {
                    double off = itemOff.get(e.getLongKey());
                    usum += e.getDoubleValue() - intercept - off;
                }

                map.put(user.getId(), usum / (uvec.size() + damping));
            }
        }

        return new UserItemBiasModel(intercept, map, itemOff);
    }
}

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
import org.lenskit.util.math.Vectors;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Compute a bias model that returns users' average ratings.  For a user \\(u\\), the global bias \\(b\\) plus the
 * user bias \\(b_u\\) will equal the user's average rating.  Item biases are all zero.
 */
public class UserAverageRatingBiasModelProvider implements Provider<UserBiasModel> {
    private final RatingVectorPDAO dao;
    private final double damping;

    @Inject
    public UserAverageRatingBiasModelProvider(@Transient RatingVectorPDAO dao, @BiasDamping double damp) {
        this.dao = dao;
        damping = damp;
    }

    @Override
    public UserBiasModel get() {
        double sum = 0;
        int n = 0;
        Long2DoubleMap sums = new Long2DoubleOpenHashMap();
        Long2DoubleMap counts = new Long2DoubleOpenHashMap();
        try (ObjectStream<IdBox<Long2DoubleMap>> stream = dao.streamUsers()) {
            for (IdBox<Long2DoubleMap> user : stream) {
                Long2DoubleMap uvec = user.getValue();
                double usum = Vectors.sum(uvec);
                int ucount = uvec.size();

                sum += usum;
                n += ucount;

                sums.put(user.getId(), usum);
                counts.put(user.getId(), ucount);
            }
        }

        double mean = n > 0 ? sum / n : 0;
        Long2DoubleMap offsets = new Long2DoubleOpenHashMap(sums.size());
        for (Long2DoubleMap.Entry e: sums.long2DoubleEntrySet()) {
            long user = e.getLongKey();
            double usum = e.getDoubleValue();
            double ucount = counts.get(user);
            usum += damping * mean;
            offsets.put(user, usum / (ucount + damping) - mean);
        }

        return new UserBiasModel(mean, offsets);
    }
}

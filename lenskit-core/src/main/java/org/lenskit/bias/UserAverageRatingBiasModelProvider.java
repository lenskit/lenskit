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

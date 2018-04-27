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

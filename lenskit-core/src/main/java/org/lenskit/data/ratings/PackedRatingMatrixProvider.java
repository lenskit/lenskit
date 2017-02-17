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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.longs.*;
import org.lenskit.inject.Transient;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Random;

/**
 * Build a packed rating matrix from the available rating events.
 */
public class PackedRatingMatrixProvider implements Provider<PackedRatingMatrix> {
    private static final Logger logger = LoggerFactory.getLogger(PackedRatingMatrixProvider.class);

    private final RatingVectorPDAO dao;
    private Random random;

    @Inject
    public PackedRatingMatrixProvider(@Transient RatingVectorPDAO dao, Random random) {
        this.dao = dao;
        this.random = random;
    }

    @Override
    public PackedRatingMatrix get() {
        logger.debug("Packing preference snapshot");

        PackedRatingDataBuilder bld = new PackedRatingDataBuilder();

        // Track the indices where everything appears for finding previous
        // rating info for a user-item pair
        Long2ObjectMap<Long2IntMap> uiIndexes =
                new Long2ObjectOpenHashMap<>(2000);

        // Since we iterate in timestamp order, we can just overwrite
        // old data for a user-item pair with new data.
        try (ObjectStream<IdBox<Long2DoubleMap>> ratings = dao.streamUsers()) {
            for (IdBox<Long2DoubleMap> user: ratings) {
                final long uid = user.getId();

                // create an item->index map for this user
                Long2IntMap imap = new Long2IntOpenHashMap();
                imap.defaultReturnValue(-1);
                uiIndexes.put(uid, imap);

                for (Long2DoubleMap.Entry r: user.getValue().long2DoubleEntrySet()) {
                    // add the rating
                    long iid = r.getLongKey();
                    int idx = bld.add(uid, iid, r.getDoubleValue());
                    imap.put(iid, idx);
                }
            }

            logger.debug("Packed {} ratings", bld.size());
        }

        bld.shuffle(random);
        PackedRatingData data = bld.build();

        return new PackedRatingMatrix(data);
    }
}
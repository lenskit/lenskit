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

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.inject.Transient;
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

    private final DataAccessObject dao;
    private Random random;

    @Inject
    public PackedRatingMatrixProvider(@Transient DataAccessObject dao, Random random) {
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
        try (ObjectStream<Rating> ratings = dao.query(Rating.class).stream()) {
            for (Rating r : ratings) {
                final long user = r.getUserId();
                final long item = r.getItemId();

                // get the item -> index map for this user
                Long2IntMap imap = uiIndexes.get(user);
                if (imap == null) {
                    imap = new Long2IntOpenHashMap();
                    imap.defaultReturnValue(-1);
                    uiIndexes.put(user, imap);
                }

                // we should never have seen this item before
                assert imap.get(item) < 0;

                // add the rating
                int idx = bld.add(r);
                imap.put(item, idx);
            }

            logger.debug("Packed {} ratings", bld.size());
        }

        bld.shuffle(random);
        PackedRatingData data = bld.build();

        return new PackedRatingMatrix(data);
    }
}

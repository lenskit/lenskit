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
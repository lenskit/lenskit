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
package org.lenskit.data.ratings;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.HashKeyIndex;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

/**
 * Rating vector source that extracts user ratings from the database.
 */
@ThreadSafe
public class StandardRatingVectorDAO implements RatingVectorDAO {
    private DataAccessObject dao;
    private volatile IdBox<Long2DoubleMap> cachedValue;

    /**
     * Construct a rating vector source.
     * @param dao The data access object.
     */
    @Inject
    public StandardRatingVectorDAO(DataAccessObject dao) {
        this.dao = dao;
    }

    @Nonnull
    @Override
    public Long2DoubleMap userRatingVector(long user) {
        IdBox<Long2DoubleMap> cached = cachedValue;
        if (cached != null && cached.getId() == user) {
            return cached.getValue();
        }

        HashKeyIndex items = new HashKeyIndex();
        DoubleList ratings = new DoubleArrayList();

        try (ObjectStream<Rating> stream = dao.query(Rating.class)
                                              .withAttribute(CommonAttributes.USER_ID, user)
                                              .stream()) {
            for (Rating r: stream) {
                int idx = items.internId(r.getItemId());
                if (idx >= ratings.size()) {
                    assert idx == ratings.size();
                    ratings.add(r.getValue());
                } else {
                    ratings.set(idx, r.getValue());
                }
            }
        }

        Long2DoubleMap map = Long2DoubleSortedArrayMap.fromArray(items, ratings);
        cachedValue = IdBox.create(user, map);
        return map;
    }
}

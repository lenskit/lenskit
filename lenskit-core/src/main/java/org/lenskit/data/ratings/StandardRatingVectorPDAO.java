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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nonnull;
import net.jcip.annotations.ThreadSafe;
import javax.inject.Inject;
import java.util.List;

/**
 * Rating vector source that extracts user ratings from the database.
 */
@ThreadSafe
public class StandardRatingVectorPDAO implements RatingVectorPDAO {
    private final DataAccessObject dao;
    private volatile IdBox<Long2DoubleMap> cachedValue;

    /**
     * Construct a rating vector source.
     * @param dao The data access object.
     */
    @Inject
    public StandardRatingVectorPDAO(DataAccessObject dao) {
        this.dao = dao;
    }

    @Nonnull
    @Override
    public Long2DoubleMap userRatingVector(long user) {
        IdBox<Long2DoubleMap> cached = cachedValue;
        if (cached != null && cached.getId() == user) {
            return cached.getValue();
        }

        Long2DoubleMap map;

        try (ObjectStream<Rating> stream = dao.query(Rating.class)
                                              .withAttribute(CommonAttributes.USER_ID, user)
                                              .stream()) {
            map = Ratings.userRatingVector(stream);
        }

        return map;
    }

    @Override
    public ObjectStream<IdBox<Long2DoubleMap>> streamUsers() {
        ObjectStream<IdBox<List<Rating>>> stream = dao.query(Rating.class)
                                                      .groupBy(CommonAttributes.USER_ID)
                                                      .stream();
        return ObjectStreams.wrap(stream.map(u -> u.mapValue(Ratings::userRatingVector)),
                                  stream);
    }
}

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

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

import com.google.common.base.Function;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Base class for helping construct rating vector proxy DAOs.
 */
public abstract class AbstractRatingVectorPDAO implements RatingVectorPDAO {
    protected final DataAccessObject dao;
    private volatile IdBox<Long2DoubleMap> cachedValue;

    public AbstractRatingVectorPDAO(DataAccessObject dao) {
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
        List<Entity> entities = dao.query(getEntityType())
                .withAttribute(CommonAttributes.USER_ID, user)
                .get();

        map = makeVector(entities);
        cachedValue = IdBox.create(user, map);

        return map;
    }

    /**
     * Get the desired type of entities.
     * @return The desired type of entities.
     */
    protected abstract EntityType getEntityType();

    /**
     * Make a vector from entities.
     * @param entities The entities.
     * @return The vector.
     */
    protected abstract Long2DoubleMap makeVector(List<Entity> entities);

    @Override
    public ObjectStream<IdBox<Long2DoubleMap>> streamUsers() {
        ObjectStream<IdBox<List<Entity>>> stream = dao.query(getEntityType())
                                                      .groupBy(CommonAttributes.USER_ID)
                                                      .stream();
        return ObjectStreams.transform(stream, new Function<IdBox<List<Entity>>, IdBox<Long2DoubleMap>>() {
            @Nullable
            @Override
            public IdBox<Long2DoubleMap> apply(@Nullable IdBox<List<Entity>> input) {
                if (input == null) {
                    return null;
                }

                return IdBox.create(input.getId(),
                                    makeVector(input.getValue()));
            }
        });
    }
}

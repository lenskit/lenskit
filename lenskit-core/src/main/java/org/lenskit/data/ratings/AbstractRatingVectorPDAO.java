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

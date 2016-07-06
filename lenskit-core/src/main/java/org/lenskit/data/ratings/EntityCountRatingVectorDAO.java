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

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.inject.Parameter;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.keys.Long2DoubleSortedArrayMap;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Rating vector DAO that counts entities appearing for a user.
 */
@ThreadSafe
public class EntityCountRatingVectorDAO implements RatingVectorDAO {
    private final DataAccessObject dao;
    private final EntityType type;
    private volatile IdBox<Long2DoubleMap> cachedValue;

    /**
     * Qualifier for the type of entities that are counted to compute user preferences.
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    @Parameter(ElementType.class)
    @Documented
    public static @interface CountedType {}

    /**
     * Construct a rating vector source.
     * @param dao The data access object.
     * @param type
     */
    @Inject
    public EntityCountRatingVectorDAO(DataAccessObject dao, @CountedType EntityType type) {
        this.dao = dao;
        this.type = type;
    }

    @Nonnull
    @Override
    public Long2DoubleMap userRatingVector(long user) {
        IdBox<Long2DoubleMap> cached = cachedValue;
        if (cached != null && cached.getId() == user) {
            return cached.getValue();
        }

        Long2DoubleMap counts = new Long2DoubleOpenHashMap();
        counts.defaultReturnValue(0);

        try (ObjectStream<Entity> stream = dao.query(type)
                                              .withAttribute(CommonAttributes.USER_ID, user)
                                              .stream()) {
            for (Entity e: stream) {
                long item = e.getLong(CommonAttributes.ITEM_ID);
                counts.put(item, counts.get(item) + 1);
            }
        }

        Long2DoubleMap map = new Long2DoubleSortedArrayMap(counts);
        cachedValue = IdBox.create(user, map);
        return map;
    }
}

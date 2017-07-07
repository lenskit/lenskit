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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.IdBox;

import net.jcip.annotations.ThreadSafe;
import javax.inject.Inject;
import java.util.List;

/**
 * Rating vector DAO that counts entities appearing for a user.
 */
@ThreadSafe
public class EntityCountRatingVectorPDAO extends AbstractRatingVectorPDAO {
    private final EntityType type;

    /**
     * Construct a rating vector source.
     * @param dao The data access object.
     * @param type An entity type.  Entities of this type should have {@link CommonAttributes#USER_ID} and
     *             {@link CommonAttributes#ITEM_ID} attributes, and a rating vector will makeVector the number
     *             of times an item ID appears for a given user ID.
     */
    @Inject
    public EntityCountRatingVectorPDAO(DataAccessObject dao, @InteractionEntityType EntityType type) {
        super(dao);
        this.type = type;
    }

    @Override
    protected EntityType getEntityType() {
        return type;
    }

    @Override
    protected Long2DoubleMap makeVector(List<Entity> entities) {
        Long2DoubleMap counts = new Long2DoubleOpenHashMap();
        counts.defaultReturnValue(0);

        for (Entity e: entities) {
            long item = e.getLong(CommonAttributes.ITEM_ID);
            counts.put(item, counts.get(item) + 1);
        }

        return counts;
    }
}

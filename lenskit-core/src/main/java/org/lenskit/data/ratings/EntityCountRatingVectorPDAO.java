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

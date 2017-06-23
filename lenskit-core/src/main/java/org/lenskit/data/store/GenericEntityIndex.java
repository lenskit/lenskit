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
package org.lenskit.data.store;

import com.google.common.collect.ImmutableListMultimap;
import org.lenskit.data.entities.Entity;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * Generic implementation of the entity index.
 */
class GenericEntityIndex implements EntityIndex {
    private ImmutableListMultimap<Object,Entity> entities;

    GenericEntityIndex(ImmutableListMultimap<Object,Entity> data) {
        entities = data;
    }

    @Nonnull
    @Override
    public List<Entity> getEntities(@Nonnull Object value) {
        return entities.get(value);
    }

    @Override
    public Set<?> getValues() {
        return entities.keySet();
    }
}

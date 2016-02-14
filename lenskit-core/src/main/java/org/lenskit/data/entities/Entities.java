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
package org.lenskit.data.entities;

import java.util.HashMap;

public final class Entities {
    private Entities() {}

    /**
     * Create a new bare entity.
     * @param type The bare entity.
     * @param id The entity ID.
     * @return An entity.
     */
    public static Entity create(EntityType type, long id) {
        return new BareEntity(type, id);
    }

    /**
     * Create a new, empty entity builder.
     * @return An empty entity builder.
     */
    public static EntityBuilder newBuilder() {
        return new BasicEntityBuilder();
    }

    /**
     * Create a new basic entity builder.
     * @param type The entity type.
     * @return The entity builder.
     */
    public static EntityBuilder newBuilder(EntityType type) {
        return new BasicEntityBuilder(type, -1, false, new HashMap<Attribute<?>, Object>());
    }

    /**
     * Create a new basic entity builder.
     * @param id The entity ID.
     * @return The entity builder.
     */
    public static EntityBuilder newBuilder(EntityType type, long id) {
        return new BasicEntityBuilder(type, id, true, new HashMap<Attribute<?>, Object>());
    }

    /**
     * Create a new entity builder that is initialized with a copy of an entity.
     * @param e The entity.
     * @return An entity builder initialized to build a copy of {@code e}.
     */
    public static EntityBuilder copyBuilder(Entity e) {
        EntityBuilder eb = newBuilder(e.getType(), e.getId());
        for (Attribute a: e.getAttributes()) {
            eb.setAttribute(a, e.get(a));
        }
        return eb;
    }
}

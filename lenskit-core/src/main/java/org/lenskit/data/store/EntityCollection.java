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
package org.lenskit.data.store;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.lenskit.data.dao.SortKey;
import org.lenskit.data.entities.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractCollection;
import java.util.List;
import java.util.Map;

/**
 * A collection of entities of a single type.  This collection augments the `Collection` interface with logic for
 * different kinds of (possibly optimized) entity searches.
 */
public abstract class EntityCollection extends AbstractCollection<Entity> {
    EntityCollection() {}

    /**
     * Create a new entity collection builder.
     * @return The builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type) {
        return new MapEntityCollectionBuilder(type);
    }

    /**
     * Create a new packed entity collection builder.
     * @param type The entity type.
     * @param attrs The attributes to store.
     * @return An entity collection builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type, AttributeSet attrs) {
        Preconditions.checkArgument(attrs.lookup(CommonAttributes.ENTITY_ID) == 0,
                                    "could not find entity ID in: %s", attrs);
        if (attrs.size() > 1) {
            return new PackedEntityCollectionBuilder(type, attrs, null);
        } else {
            return new BareEntityCollectionBuilder(type);
        }
    }

    /**
     * Create a new packed entity collection builder.
     * @param type The entity type.
     * @param attrs The attributes to store.
     * @param eb An entity builder to use when reconstituting entities.
     * @return An entity collection builder.
     */
    public static EntityCollectionBuilder newBuilder(EntityType type, AttributeSet attrs, Class<? extends EntityBuilder> eb) {
        Preconditions.checkArgument(attrs.lookup(CommonAttributes.ENTITY_ID) == 0,
                                    "could not find entity ID in: %s", attrs);
        if (attrs.size() > 1) {
            return new PackedEntityCollectionBuilder(type, attrs, eb);
        } else {
            return new BareEntityCollectionBuilder(type);
        }
    }


    /**
     * Create a new builder for colletions of bare entitites (only storing IDs).
     * @param type The entity type.
     * @return The builder.
     */
    public static EntityCollectionBuilder newBareBuilder(EntityType type) {
        return new BareEntityCollectionBuilder(type);
    }

    /**
     * Get the type of entity stored in this collection.
     * @return The entity type this collection stores.
     */
    public abstract EntityType getType();

    public abstract LongSet idSet();

    /**
     * Look up an entity by ID.
     * @param id The entity ID.
     * @return The entity, or `null` if no such entity exists.
     */
    @Nullable
    public abstract Entity lookup(long id);

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public abstract <T> List<Entity> find(TypedName<T> name, T value);

    /**
     * Find entities with an attribute.
     * @param attr The attribute to look for.
     * @param <T> The attribute type.
     * @return A list of entities with the specified attribute.
     */
    @Nonnull
    public abstract <T> List<Entity> find(Attribute<T> attr);

    /**
     * Find entities by attribute.
     * @param name The attribute name.
     * @param value The attribute value.
     * @return A list of entities for which attribute `name` has value `value`.
     */
    @Nonnull
    public abstract List<Entity> find(String name, Object value);

    /**
     * Get a grouped view of the data.
     * @param attr The grouping attribute.
     * @return A map of values to entities with that value in the specified attribute.
     */
    public abstract Map<Long,List<Entity>> grouped(TypedName<Long> attr);

    /**
     * Get the sort keys, if this collection stores attributes in sorted order.
     * @return The sort keys.
     */
    public List<SortKey> getSortKeys() {
        return ImmutableList.of();
    }
}

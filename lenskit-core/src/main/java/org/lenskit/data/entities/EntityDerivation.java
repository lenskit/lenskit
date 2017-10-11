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
package org.lenskit.data.entities;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * Describe a derivation of one entity from another.  This is used to extract bare entities from references in
 * other entity types, e.g. user IDs in ratings, so that you don't have to have explicit data source for every
 * type of entity in the system.
 */
public class EntityDerivation {
    private final EntityType type;
    private final EntityType sourceType;
    private final TypedName<Long> attribute;

    private EntityDerivation(EntityType t, EntityType src, TypedName<Long> attr) {
        type = t;
        sourceType = src;
        attribute = attr;
    }

    /**
     * Create a new entity derivation.
     * @param t The derived entity type.
     * @param src The source type.
     * @param attr The attribute to derive from.
     * @return An entity derivation.
     */
    public static EntityDerivation create(EntityType t, EntityType src, TypedName<Long> attr) {
        return new EntityDerivation(t, src, attr);
    }

    /**
     * Get the entity type to be derived.
     * @return The entity type to derive.
     */
    public EntityType getType() {
        return type;
    }

    /**
     * Get the source types for the derivation.
     * @return The source types for the derivation.
     */
    public EntityType getSourceType() {
        return sourceType;
    }

    /**
     * Get the source attribute for the derivation.
     * @return The attribute whose value contains IDs of the derived entity.
     */
    public TypedName<Long> getAttribute() {
        return attribute;
    }

    /**
     * Parse an entity derivation from a JSON node.
     * @param node The JSON node.
     * @return The entity derivation.
     */
    public static EntityDerivation fromJSON(JsonNode node) {
        JsonNode src = node.get("source_type");
        Preconditions.checkArgument(src != null, "missing source_type");
        JsonNode tgt = node.get("entity_type");
        Preconditions.checkArgument(tgt != null, "missing entity_type");
        JsonNode attr = node.get("source_attribute");
        Preconditions.checkArgument(attr != null, "missing source_attribute");

        return create(EntityType.forName(tgt.asText()),
                      EntityType.forName(src.asText()),
                      TypedName.create(attr.asText(), Long.class));
    }
}

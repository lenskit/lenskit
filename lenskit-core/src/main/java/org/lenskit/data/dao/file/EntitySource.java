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
package org.lenskit.data.dao.file;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.lenskit.data.entities.AttributeSet;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityBuilder;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Interface for entity providers.
 */
public interface EntitySource {
    /**
     * Get the name of this entity source.
     * @return The entity source name.
     */
    @Nonnull
    String getName();

    /**
     * Get the entity types produced by this source.
     * @return The set of entity types produced by this source.
     */
    @Nonnull
    Set<EntityType> getTypes();

    /**
     * Get this entity source's layout, if one is available.  A source has a layout if it produces a single entity
     * type with a known set of attributes.
     *
     * @return The layout, or `null` if this source does not know its layout.
     */
    @Nullable
    Layout getLayout();

    /**
     * Get the data from this entity source.
     * @return The data from the entity source.
     */
    @Nonnull
    ObjectStream<Entity> openStream() throws IOException;

    /**
     * Get metadata from this entity source.
     */
    @Nonnull
    Map<String,Object> getMetadata();

    /**
     * The layout of an entity source.
     */
    class Layout {
        private final EntityType entityType;
        private final AttributeSet attributes;
        private final Class<? extends EntityBuilder> entityBuilder;

        /**
         * Construct an entity source layout.
         * @param et The entity type.
         * @param attrs The attributes.
         * @param eb The entity builder class.
         */
        public Layout(EntityType et, AttributeSet attrs, Class<? extends EntityBuilder> eb) {
            entityType = et;
            attributes = attrs;
            entityBuilder = eb;
        }

        public EntityType getEntityType() {
            return entityType;
        }

        public AttributeSet getAttributes() {
            return attributes;
        }

        public Class<? extends EntityBuilder> getEntityBuilder() {
            return entityBuilder;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            Layout layout = (Layout) o;

            return new EqualsBuilder()
                    .append(entityType, layout.entityType)
                    .append(attributes, layout.attributes)
                    .append(entityBuilder, layout.entityBuilder)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(entityType)
                    .append(attributes)
                    .append(entityBuilder)
                    .toHashCode();
        }
    }
}

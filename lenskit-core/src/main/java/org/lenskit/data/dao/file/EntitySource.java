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
package org.lenskit.data.dao.file;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                    .append("type", entityType)
                    .append("attributes", attributes)
                    .build();
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

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
package org.lenskit.data.entities;

import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import java.util.Collections;
import java.util.Set;

/**
 * Implement a bare entity that has no attributes.
 */
@Immutable
class BareEntity extends AbstractEntity {
    static final Set<String> ATTRIBUTE_NAMES = Collections.singleton("id");
    static final Set<TypedName<?>> TYPED_ATTRIBUTE_NAMES = Collections.<TypedName<?>>singleton(CommonAttributes.ENTITY_ID);

    BareEntity(EntityType t, long eid) {
        super(t, eid);
    }

    @Override
    public Set<String> getAttributeNames() {
        return ATTRIBUTE_NAMES;
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        return TYPED_ATTRIBUTE_NAMES;
    }

    @Override
    public boolean hasAttribute(String name) {
        return "id".equals(name);
    }

    @Override
    public boolean hasAttribute(TypedName<?> name) {
        return name == CommonAttributes.ENTITY_ID;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        return name == CommonAttributes.ENTITY_ID
                ? (T) name.getRawType().cast(getId())
                : null;
    }

    @Nullable
    @Override
    public Object maybeGet(String attr) {
        return "id".equals(attr) ? getId() : null;
    }
}

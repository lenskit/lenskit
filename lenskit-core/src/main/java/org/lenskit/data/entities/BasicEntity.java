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

import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import net.jcip.annotations.Immutable;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Implementation of an entity backed by a basic set of attributes.
 */
@Immutable
class BasicEntity extends AbstractEntity {
    private final AttributeSet attributeNames;
    private final Object[] attributeValues;

    BasicEntity(EntityType t, long eid, AttributeSet aset, Object[] avals) {
        super(t, eid);
        attributeNames = aset;
        attributeValues = avals;
    }

    @Override
    public Set<String> getAttributeNames() {
        return attributeNames.nameSet();
    }

    @Override
    public Set<TypedName<?>> getTypedAttributeNames() {
        return attributeNames;
    }

    @Override
    public Collection<Attribute<?>> getAttributes() {
        return new AbstractCollection<Attribute<?>>() {
            @Override
            public Iterator<Attribute<?>> iterator() {
                return (Iterator) IntStream.range(0, attributeNames.size())
                                           .mapToObj(i -> {
                                               if (i == 0) {
                                                   return Attribute.create(CommonAttributes.ENTITY_ID, getId());
                                               }
                                               Object val = attributeValues[i-1];
                                               if (val == null) {
                                                   return null;
                                               } else {
                                                   return Attribute.create((TypedName) attributeNames.getAttribute(i), val);
                                               }
                                           })
                                           .filter(Predicates.notNull())
                                           .iterator();
            }

            @Override
            public int size() {
                return attributeNames.size();
            }
        };
    }

    @Override
    public boolean hasAttribute(String name) {
        return attributeNames.lookup(name) >= 0;
    }

    @Override
    public boolean hasAttribute(TypedName<?> name) {
        return attributeNames.contains(name);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T maybeGet(TypedName<T> name) {
        int idx = attributeNames.lookup(name);
        if (idx < 0) {
            return null;
        } else if (idx == 0) {
            return (T) name.getRawType().cast(getId());
        } else {
            return (T) name.getRawType().cast(attributeValues[idx-1]);
        }
    }

    @Nullable
    @Override
    public Object maybeGet(String name) {
        int idx = attributeNames.lookup(name);
        if (idx < 0) {
            return null;
        } else if (idx == 0) {
            return getId();
        } else {
            return attributeValues[idx-1];
        }
    }
}

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

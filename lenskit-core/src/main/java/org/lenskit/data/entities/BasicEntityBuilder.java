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

import com.google.common.base.Preconditions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * General-purpose builder for {@linkplain Entity entities}.
 */
public class BasicEntityBuilder extends EntityBuilder {
    private Map<String,Attribute<?>> attributes;

    /**
     * Create a new entity builder for a type.
     * @param type The entity type.
     */
    public BasicEntityBuilder(EntityType type) {
        super(type);
        attributes = new LinkedHashMap<>();
    }

    @Override
    public <T> EntityBuilder setAttribute(TypedName<T> name, T val) {
        Preconditions.checkNotNull(name, "attribute");
        Preconditions.checkNotNull(val, "value");
        Preconditions.checkArgument(name.getRawType().isInstance(val),
                                    "value %s not of type %s", val, name.getType());
        if (name == CommonAttributes.ENTITY_ID) {
            return setId(((Long) val).longValue());
        } else {
            attributes.put(name.getName(), Attribute.create(name, val));
            return this;
        }
    }

    @Override
    public EntityBuilder clearAttribute(TypedName<?> name) {
        Preconditions.checkNotNull(name, "attribute");
        attributes.remove(name.getName());
        if (name == CommonAttributes.ENTITY_ID) {
            idSet = false;
        }
        return this;
    }

    @Override
    public Entity build() {
        Preconditions.checkState(type != null, "Entity type not set");
        Preconditions.checkState(idSet, "ID not set");
        if (attributes.isEmpty() || attributes.keySet().equals(Collections.singleton(CommonAttributes.ENTITY_ID))) {
            return new BareEntity(type, id);
        } else {
            List<TypedName<?>> names = new ArrayList<>(attributes.size());
            names.add(CommonAttributes.ENTITY_ID);
            for (Attribute<?> a: attributes.values()) {
                names.add(a.getTypedName());
            }
            assert names.lastIndexOf(CommonAttributes.ENTITY_ID) == 0;

            AttributeSet aset = AttributeSet.create(names);
            Object[] values = new Object[aset.size()];
            for (Attribute<?> a: attributes.values()) {
                int i = aset.lookup(a.getTypedName());
                values[i-1] = a.getValue();
            }

            return new BasicEntity(type, id, aset, values);
        }
    }
}

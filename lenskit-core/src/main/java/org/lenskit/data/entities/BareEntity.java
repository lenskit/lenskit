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

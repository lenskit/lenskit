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

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test building various kinds of entities.
 */
public class EntityBuilderTest {
    @Test
    public void testBareEntity() {
        EntityBuilder eb = Entities.newBuilder(CommonTypes.USER, 42);
        Entity e = eb.build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getTypedAttributeNames(), contains((TypedName) CommonAttributes.ENTITY_ID));
        assertThat(e.getAttributeNames(), contains("id"));
        assertThat(e.get(CommonAttributes.ENTITY_ID), equalTo(42L));
        assertThat(e.get("id"), equalTo((Object) 42L));
    }

    @Test
    public void testBareSetters() {
        EntityBuilder eb = Entities.newBuilder(CommonTypes.USER)
                                   .setId(42);
        Entity e = eb.build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getTypedAttributeNames(), contains((TypedName) CommonAttributes.ENTITY_ID));
        assertThat(e.getAttributeNames(), contains("id"));
        assertThat(e.hasAttribute("user"), equalTo(false));
        assertThat(e.hasAttribute(CommonAttributes.USER_ID), equalTo(false));
    }

    @Test
    public void testAttributeSetters() {
        EntityBuilder eb = Entities.newBuilder(CommonTypes.USER)
                                   .setAttribute(CommonAttributes.ENTITY_ID, 42L);
        Entity e = eb.build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getTypedAttributeNames(), contains((TypedName) CommonAttributes.ENTITY_ID));
        assertThat(e.getAttributeNames(), contains("id"));
        assertThat(e.hasAttribute("user"), equalTo(false));
        assertThat(e.hasAttribute(CommonAttributes.USER_ID), equalTo(false));
        assertThat(e.get(CommonAttributes.ENTITY_ID), equalTo(42L));
    }

    @Test
    public void testBasicEntity() {
        Entity e = Entities.newBuilder(CommonTypes.USER, 42)
                           .setAttribute(CommonAttributes.NAME, "HACKEM MUCHE")
                           .build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getTypedAttributeNames(), containsInAnyOrder((TypedName) CommonAttributes.NAME,
                                                                  CommonAttributes.ENTITY_ID));
        assertThat(e.getAttributeNames(), containsInAnyOrder("name", "id"));
        assertThat(e.get("name"), equalTo((Object) "HACKEM MUCHE"));
        assertThat(e.get(CommonAttributes.NAME), equalTo("HACKEM MUCHE"));
        assertThat(e.hasAttribute("name"), equalTo(true));
        assertThat(e.hasAttribute("user"), equalTo(false));
        assertThat(e.hasAttribute(CommonAttributes.NAME), equalTo(true));
        assertThat(e.hasAttribute(CommonAttributes.USER_ID), equalTo(false));
        assertThat(e.get(CommonAttributes.ENTITY_ID), equalTo(42L));
    }
}

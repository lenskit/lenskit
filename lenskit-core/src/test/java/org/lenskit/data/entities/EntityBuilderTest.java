/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * Test building various kinds of entities.
 */
public class EntityBuilderTest {
    @Test
    public void testBareEntity() {
        EntityBuilder eb = EntityBuilder.create(CommonTypes.USER, 42);
        Entity e = eb.build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getAttributes(), hasSize(0));
        assertThat(e.getAttributeNames(), hasSize(0));
    }

    @Test
    public void testBareSetters() {
        EntityBuilder eb = EntityBuilder.create()
                                        .setType(CommonTypes.USER)
                                        .setId(42);
        Entity e = eb.build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getAttributes(), hasSize(0));
        assertThat(e.getAttributeNames(), hasSize(0));
    }

    @Test
    public void testBasicEntity() {
        Entity e = EntityBuilder.create(CommonTypes.USER, 42)
                                .setAttribute(CommonAttributes.NAME, "HACKEM MUCHE")
                                .build();
        assertThat(e, notNullValue());
        assertThat(e.getType(), equalTo(CommonTypes.USER));
        assertThat(e.getId(), equalTo(42L));
        assertThat(e.getAttributes(), contains((Attribute) CommonAttributes.NAME));
        assertThat(e.getAttributeNames(), contains("name"));
        assertThat(e.get("name"), equalTo((Object) "HACKEM MUCHE"));
        assertThat(e.get(CommonAttributes.NAME), equalTo("HACKEM MUCHE"));
    }
}

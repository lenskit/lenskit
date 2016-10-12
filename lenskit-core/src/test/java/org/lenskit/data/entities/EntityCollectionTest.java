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

import com.google.common.collect.Lists;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * Created by MichaelEkstrand on 6/18/2016.
 */
public class EntityCollectionTest {
    @Test
    public void testEmptyBuilder() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER)
                                              .build();
        assertThat(ec.size(), equalTo(0));
        assertThat(ec.lookup(42), nullValue());
        assertThat(ec.getType(), equalTo(CommonTypes.USER));
        assertThat(ec.idSet(), hasSize(0));
    }

    @Test
    public void testAddEntity() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER)
                                              .add(Entities.create(CommonTypes.USER, 42))
                                              .build();
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(42),
                   equalTo(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.lookup(70), nullValue());
        assertThat(Lists.newArrayList(ec),
                   contains(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.find(CommonAttributes.ENTITY_ID, 42L),
                   contains(Entities.create(CommonTypes.USER, 42)));
        assertThat(ec.getType(), equalTo(CommonTypes.USER));
        assertThat(ec.idSet(), contains(42L));
    }

    @Test
    public void testFindEntity() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .add(rating)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }

    @Test
    public void testFindIndexedEntity() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .addIndex(CommonAttributes.USER_ID)
                                              .add(rating)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }

    @Test
    public void testIndexEntityAfterAddStarted() {
        Entity rating = Entities.newBuilder(CommonTypes.RATING)
                                .setId(37)
                                .setAttribute(CommonAttributes.USER_ID, 10L)
                                .setAttribute(CommonAttributes.ITEM_ID, 203L)
                                .setAttribute(CommonAttributes.RATING, 3.5)
                                .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING)
                                              .add(rating)
                                              .addIndex(CommonAttributes.USER_ID)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
        assertThat(ec.lookup(37),
                   equalTo(rating));
        assertThat(Lists.newArrayList(ec),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.USER_ID, 10L),
                   contains(rating));
        assertThat(ec.find(CommonAttributes.ITEM_ID, 10L),
                   hasSize(0));
    }
}

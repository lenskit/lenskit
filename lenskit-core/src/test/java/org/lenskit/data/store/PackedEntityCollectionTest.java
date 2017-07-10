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
package org.lenskit.data.store;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * Created by MichaelEkstrand on 6/18/2016.
 */
public class PackedEntityCollectionTest {
    @Test
    public void testEmptyBuilder() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID))
                                              .build();
        assertThat(ec.size(), equalTo(0));
        assertThat(ec.lookup(42), nullValue());
        assertThat(ec.getType(), equalTo(CommonTypes.USER));
        assertThat(ec.idSet(), hasSize(0));
    }

    @Test
    public void testAddEntity() {
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.USER,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID))
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
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                                              CommonAttributes.USER_ID,
                                                                              CommonAttributes.ITEM_ID,
                                                                              CommonAttributes.RATING))
                                              .add(rating)
                                              .build();
        assertThat(ec.getType(), equalTo(CommonTypes.RATING));
        assertThat(ec.size(), equalTo(1));
//        assertThat(ec.lookup(37), instanceOf(Rating.class));
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
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                                              CommonAttributes.USER_ID,
                                                                              CommonAttributes.ITEM_ID,
                                                                              CommonAttributes.RATING))
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
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                                              CommonAttributes.USER_ID,
                                                                              CommonAttributes.ITEM_ID,
                                                                              CommonAttributes.RATING))
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

    @Test
    public void testGroupEntities() {
        EntityFactory efac = new EntityFactory();
        Rating r1 = efac.rating(100, 200, 3.5);
        Rating r2 = efac.rating(100, 201, 4.0);
        Rating r3 = efac.rating(101, 200, 2.0);
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING,
                                                          AttributeSet.create(CommonAttributes.ENTITY_ID,
                                                                              CommonAttributes.USER_ID,
                                                                              CommonAttributes.ITEM_ID,
                                                                              CommonAttributes.RATING))
                                              .add(r1)
                                              .add(r2)
                                              .add(r3)
                                              .addIndex(CommonAttributes.USER_ID)
                                              .build();

        Map<Long,List<Entity>> groups = ec.grouped(CommonAttributes.USER_ID);
        assertThat(groups.keySet(), containsInAnyOrder(100L, 101L));
        assertThat(groups, hasEntry(equalTo(101L), contains(r3)));
        assertThat(groups, hasEntry(equalTo(100L), containsInAnyOrder(r1, r2)));

        groups = ec.grouped(CommonAttributes.ITEM_ID);
        assertThat(groups.keySet(), containsInAnyOrder(200L, 201L));
        assertThat(groups, hasEntry(equalTo(201L), contains(r2)));
        assertThat(groups, hasEntry(equalTo(200L), containsInAnyOrder(r1, r3)));
    }

    @Test
    public void testWithMissingAttribute() {
        Rating r = Rating.newBuilder()
                         .setId(42)
                         .setUserId(100)
                         .setItemId(50)
                         .setRating(3.5)
                         .build();
        EntityCollection ec = EntityCollection.newBuilder(CommonTypes.RATING, Rating.ATTRIBUTES)
                                              .add(r)
                                              .build();
        assertThat(ec, contains(r));
    }
}

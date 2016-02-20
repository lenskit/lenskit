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
package org.lenskit.data.dao;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.lenskit.data.entities.*;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStreams;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class EntityCollectionDAOTest {
    private static final EntityType LIKE = EntityType.forName("like");

    @Test
    public void testEmptyDAO() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(CommonTypes.RATING), hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.RATING)),
                   hasSize(0));
    }

    @Test
    public void testOneEntity() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(Entities.create(CommonTypes.USER, 42));

        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(42L));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.RATING)),
                   hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.USER)),
                   contains(Entities.create(CommonTypes.USER, 42L)));
        assertThat(dao.lookupEntity(CommonTypes.USER, 42),
                   equalTo(Entities.create(CommonTypes.USER, 42)));
    }

    @Test
    public void testNullQueryOneEntity() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(Entities.create(CommonTypes.USER, 42));

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(CommonTypes.USER)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(Entities.create(CommonTypes.USER, 42)));
    }

    @Test
    public void testQueryPassOneEntity() {
        Entity e = Entities.newBuilder(LIKE, 1)
                           .setAttribute(CommonAttributes.USER_ID, 42L)
                           .setAttribute(CommonAttributes.ITEM_ID, 39L)
                           .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e);

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(e));

        query = EntityQuery.newBuilder()
                           .setEntityType(LIKE)
                           .addEqualsCondition(CommonAttributes.USER_ID, 42L)
                           .build();
        results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(e));
    }

    @Test
    public void testQueryRejectOneEntity() {
        Entity e = Entities.newBuilder(LIKE, 1)
                           .setAttribute(CommonAttributes.USER_ID, 42L)
                           .setAttribute(CommonAttributes.ITEM_ID, 39L)
                           .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e);

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .addEqualsCondition(CommonAttributes.USER_ID, 39L)
                                               .build();
        ArrayList<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));


        assertThat(results, hasSize(0));
    }

    @Test
    public void testQuerySelectEntities() {
        Entity e1 = Entities.newBuilder(LIKE, 1)
                            .setAttribute(CommonAttributes.USER_ID, 42L)
                            .setAttribute(CommonAttributes.ITEM_ID, 39L)
                            .build();
        Entity e2 = Entities.newBuilder(LIKE, 2)
                            .setAttribute(CommonAttributes.USER_ID, 42L)
                            .setAttribute(CommonAttributes.ITEM_ID, 28L)
                            .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e1, e2);

        assertThat(dao.lookupEntity(LIKE, 1),
                   equalTo(e1));
        assertThat(dao.lookupEntity(LIKE, 2),
                   equalTo(e2));
        assertThat(dao.lookupEntity(LIKE, 2, Entity.class),
                   equalTo(e2));
        assertThat(dao.lookupEntity(LIKE, 3),
                   nullValue());
        assertThat(dao.lookupEntity(CommonTypes.USER, 1),
                   nullValue());

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, containsInAnyOrder(e1, e2));

        query = EntityQuery.newBuilder()
                           .setEntityType(LIKE)
                           .addEqualsCondition(CommonAttributes.USER_ID, 42L)
                           .build();
        results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, containsInAnyOrder(e1, e2));

        query = EntityQuery.newBuilder()
                           .setEntityType(LIKE)
                           .addEqualsCondition(CommonAttributes.ITEM_ID, 39L)
                           .build();
        results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(e1));
    }

    @Test
    public void testQuerySortEntities() {
        Entity e1 = Entities.newBuilder(LIKE, 1)
                            .setAttribute(CommonAttributes.USER_ID, 42L)
                            .setAttribute(CommonAttributes.ITEM_ID, 39L)
                            .build();
        Entity e2 = Entities.newBuilder(LIKE, 2)
                            .setAttribute(CommonAttributes.USER_ID, 42L)
                            .setAttribute(CommonAttributes.ITEM_ID, 28L)
                            .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e1, e2);

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .addSortKey(CommonAttributes.ITEM_ID)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(e2, e1));
    }

    @Test
    public void testGroupOneEntity() {
        Entity e = Entities.newBuilder(LIKE, 1)
                           .setAttribute(CommonAttributes.USER_ID, 42L)
                           .setAttribute(CommonAttributes.ITEM_ID, 39L)
                           .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e);

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .build();
        ArrayList<IdBox<List<Entity>>> results =
                ObjectStreams.makeList(dao.streamEntityGroups(query, CommonAttributes.USER_ID));
        assertThat(results, hasSize(1));
        IdBox<List<Entity>> box = results.get(0);
        assertThat(box.getId(), equalTo(42L));
        assertThat(box.getValue(), contains(Entities.copyBuilder(e).build()));
    }

    @Test
    public void testGroupEntities() {
        List<Entity> entities = new ArrayList<>();
        entities.add(Entities.newBuilder(LIKE, 1)
                             .setAttribute(CommonAttributes.USER_ID, 42L)
                             .setAttribute(CommonAttributes.ITEM_ID, 39L)
                             .build());
        entities.add(Entities.newBuilder(LIKE, 2)
                             .setAttribute(CommonAttributes.USER_ID, 67L)
                             .setAttribute(CommonAttributes.ITEM_ID, 28L)
                             .build());
        entities.add(Entities.newBuilder(LIKE, 3)
                             .setAttribute(CommonAttributes.USER_ID, 42L)
                             .setAttribute(CommonAttributes.ITEM_ID, 28L)
                             .build());
        EntityCollectionDAO dao = new EntityCollectionDAO(entities);

        EntityQuery<Entity> query = EntityQuery.newBuilder()
                                               .setEntityType(LIKE)
                                               .addSortKey(CommonAttributes.ITEM_ID)
                                               .build();
        ArrayList<IdBox<List<Entity>>> results =
                ObjectStreams.makeList(dao.streamEntityGroups(query, CommonAttributes.USER_ID));
        assertThat(results, hasSize(2));
        assertThat(results,
                   containsInAnyOrder(IdBox.create(42L, (List) ImmutableList.of(entities.get(2), entities.get(1))),
                                      IdBox.create(67L, ImmutableList.of(entities.get(1)))));
    }
}

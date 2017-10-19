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
package org.lenskit.data.dao;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.Ratings;
import org.lenskit.util.IdBox;
import org.lenskit.util.io.ObjectStreams;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.someLists;
import static net.java.quickcheck.generator.PrimitiveGenerators.integers;
import static net.java.quickcheck.generator.PrimitiveGeneratorsIterables.someFixedValues;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.lenskit.data.entities.CommonTypes.RATING;
import static org.lenskit.util.test.LenskitGenerators.ratings;

public class EntityCollectionDAOTest {
    private static final EntityType LIKE = EntityType.forName("like");

    @Test
    public void testEmptyDAO() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(RATING), hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(RATING)),
                   hasSize(0));
    }

    @Test
    public void testEmptyDAOHasEmptyGroupedResults() {
        EntityCollectionDAO dao = EntityCollectionDAO.create();
        assertThat(dao.query(RATING).groupBy(CommonAttributes.ITEM_ID).get(),
                   hasSize(0));
    }

    @Test
    public void testOneEntity() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(Entities.create(CommonTypes.USER, 42));

        assertThat(dao.getEntityIds(CommonTypes.ITEM), hasSize(0));
        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(42L));
        assertThat(ObjectStreams.makeList(dao.streamEntities(RATING)),
                   hasSize(0));
        assertThat(ObjectStreams.makeList(dao.streamEntities(CommonTypes.USER)),
                   contains(Entities.create(CommonTypes.USER, 42L)));
        assertThat(dao.lookupEntity(CommonTypes.USER, 42),
                   equalTo(Entities.create(CommonTypes.USER, 42)));
    }

    @Test
    public void testNullQueryOneEntity() {
        EntityCollectionDAO dao = EntityCollectionDAO.create(Entities.create(CommonTypes.USER, 42));

        EntityQuery<Entity> query = EntityQuery.newBuilder(CommonTypes.USER)
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

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, contains(e));

        query = EntityQuery.newBuilder(LIKE)
                           .addFilterField(CommonAttributes.USER_ID, 42L)
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

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .addFilterField(CommonAttributes.USER_ID, 39L)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));


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

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .build();
        List<Entity> results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, containsInAnyOrder(e1, e2));

        query = EntityQuery.newBuilder(LIKE)
                           .addFilterField(CommonAttributes.USER_ID, 42L)
                           .build();
        results = ObjectStreams.makeList(dao.streamEntities(query));
        assertThat(results, containsInAnyOrder(e1, e2));

        query = EntityQuery.newBuilder(LIKE)
                           .addFilterField(CommonAttributes.ITEM_ID, 39L)
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

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
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

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .build();
        List<IdBox<List<Entity>>> results =
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
        EntityCollectionDAO dao = EntityCollectionDAO.create(entities);

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .addSortKey(CommonAttributes.ITEM_ID)
                                               .build();
        List<IdBox<List<Entity>>> results =
                ObjectStreams.makeList(dao.streamEntityGroups(query, CommonAttributes.USER_ID));
        assertThat(results, hasSize(2));
        assertThat(results,
                   containsInAnyOrder(IdBox.create(42L, (List) ImmutableList.of(entities.get(2), entities.get(0))),
                                      IdBox.create(67L, ImmutableList.of(entities.get(1)))));
    }

    @Test
    public void testMultipleTypes() {
        EntityCollectionDAOBuilder builder = EntityCollectionDAO.newBuilder();
        builder.addEntity(Entities.newBuilder(LIKE, 1)
                                  .setAttribute(CommonAttributes.USER_ID, 42L)
                                  .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                  .build());
        builder.addEntity(Entities.newBuilder(RATING, 10)
                                  .setAttribute(CommonAttributes.USER_ID, 42L)
                                  .setAttribute(CommonAttributes.ITEM_ID, 75L)
                                  .setAttribute(CommonAttributes.RATING, 3.5)
                                  .build());
        EntityCollectionDAO dao = builder.build();

        assertThat(dao.getEntityIds(RATING), contains(10L));
        assertThat(dao.getEntityIds(LIKE), contains(1L));
        assertThat(dao.lookupEntity(RATING, 10L),
                   equalTo(Entities.newBuilder(RATING, 10)
                                   .setAttribute(CommonAttributes.USER_ID, 42L)
                                   .setAttribute(CommonAttributes.ITEM_ID, 75L)
                                   .setAttribute(CommonAttributes.RATING, 3.5)
                                   .build()));
        assertThat(ObjectStreams.makeList(dao.streamEntities(RATING)),
                   contains(Entities.newBuilder(RATING, 10)
                                    .setAttribute(CommonAttributes.USER_ID, 42L)
                                    .setAttribute(CommonAttributes.ITEM_ID, 75L)
                                    .setAttribute(CommonAttributes.RATING, 3.5)
                                    .build()));

        assertThat(ObjectStreams.makeList(dao.streamEntities(LIKE)),
                   contains(Entities.newBuilder(LIKE, 1)
                                    .setAttribute(CommonAttributes.USER_ID, 42L)
                                    .setAttribute(CommonAttributes.ITEM_ID, 39L)
                                    .build()));

        assertThat(dao.getEntityTypes(),
                   containsInAnyOrder(RATING, LIKE));
    }

    @Test
    public void testQueryRejectOneEntityFluent() {
        Entity e = Entities.newBuilder(LIKE, 1)
                           .setAttribute(CommonAttributes.USER_ID, 42L)
                           .setAttribute(CommonAttributes.ITEM_ID, 39L)
                           .build();
        EntityCollectionDAO dao = EntityCollectionDAO.create(e);

        List<Entity> results = dao.query(LIKE)
                                  .withAttribute(CommonAttributes.USER_ID, 39L)
                                  .get();

        assertThat(results, hasSize(0));
    }

    @Test
    public void testGroupEntitiesFluently() {
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
        EntityCollectionDAO dao = EntityCollectionDAO.create(entities);

        EntityQuery<Entity> query = EntityQuery.newBuilder(LIKE)
                                               .addSortKey(CommonAttributes.ITEM_ID)
                                               .build();
        List<IdBox<List<Entity>>> results = dao.query(LIKE)
                                               .orderBy(CommonAttributes.ITEM_ID)
                                               .groupBy(CommonAttributes.USER_ID).get();
        assertThat(results, hasSize(2));
        assertThat(results,
                   containsInAnyOrder(IdBox.create(42L, (List) ImmutableList.of(entities.get(2), entities.get(0))),
                                      IdBox.create(67L, ImmutableList.of(entities.get(1)))));
    }

    @Test
    public void testAddEntityLayout() {
        EntityCollectionDAOBuilder b = EntityCollectionDAO.newBuilder();
        b.addEntityLayout(Rating.ENTITY_TYPE, Rating.ATTRIBUTES);
        Rating r = Rating.newBuilder()
                         .setId(42)
                         .setUserId(100)
                         .setItemId(50)
                         .setRating(3.5)
                         .setTimestamp(1034801)
                         .build();
        b.addEntity(r);
        EntityCollectionDAO dao = b.build();
        assertThat(dao.getEntityIds(CommonTypes.RATING), contains(42L));
        assertThat(dao.streamEntities(CommonTypes.RATING)
                      .collect(Collectors.toList()),
                   contains(r));
        assertThat(dao.query(Rating.class)
                      .get(),
                   contains(r));
    }

    @Test
    public void testStoreABunchOfRatings() {
        for (List<Rating> ratings: someLists(ratings(), integers(100, 5000))) {
            EntityCollectionDAOBuilder b = EntityCollectionDAO.newBuilder();
            b.addDefaultIndex(CommonAttributes.USER_ID);
            b.addDefaultIndex(CommonAttributes.ITEM_ID);
            ratings.forEach(b::addEntity);

            b.deriveEntities(CommonTypes.USER, CommonTypes.RATING, CommonAttributes.USER_ID);
            b.deriveEntities(CommonTypes.ITEM, CommonTypes.RATING, CommonAttributes.ITEM_ID);

            DataAccessObject dao = b.build();

            List<Rating> sorted = Entities.idOrdering().sortedCopy(ratings);
            List<Rating> built = dao.query(Rating.class)
                                    .orderBy(CommonAttributes.ENTITY_ID)
                                    .get();
            assertThat(built, hasSize(ratings.size()));
            assertThat(built, equalTo(sorted));

            Set<Long> userIds = ratings.stream().map(Rating::getUserId).collect(Collectors.toSet());
            Set<Long> itemIds = ratings.stream().map(Rating::getItemId).collect(Collectors.toSet());
            assertThat(dao.getEntityIds(CommonTypes.USER),
                       equalTo(userIds));
            assertThat(dao.getEntityIds(CommonTypes.ITEM),
                       equalTo(itemIds));

            for (long user: someFixedValues(userIds)) {
                List<Rating> fromData = ratings.stream()
                                               .filter(r -> r.getUserId() == user)
                                               .sorted(Ratings.TIMESTAMP_COMPARATOR)
                                               .collect(Collectors.toList());
                List<Rating> fromDAO = dao.query(Rating.class)
                                          .withAttribute(CommonAttributes.USER_ID, user)
                                          .orderBy(CommonAttributes.TIMESTAMP)
                                          .get();
                assertThat(fromDAO, equalTo(fromData));
            }

            for (long item: someFixedValues(itemIds)) {
                List<Rating> fromData = ratings.stream()
                                               .filter(r -> r.getItemId() == item)
                                               .sorted(Ratings.TIMESTAMP_COMPARATOR)
                                               .collect(Collectors.toList());
                List<Rating> fromDAO = dao.query(Rating.class)
                                          .withAttribute(CommonAttributes.ITEM_ID, item)
                                          .orderBy(CommonAttributes.TIMESTAMP)
                                          .get();
                assertThat(fromDAO, equalTo(fromData));
            }
        }
    }

    @Test
    public void testPackABunchOfRatings() {
        for (List<Rating> ratings: someLists(ratings(), integers(100, 5000))) {
            EntityCollectionDAOBuilder b = EntityCollectionDAO.newBuilder();
            b.addEntityLayout(Rating.ENTITY_TYPE, Rating.ATTRIBUTES);
            b.addDefaultIndex(CommonAttributes.USER_ID);
            b.addDefaultIndex(CommonAttributes.ITEM_ID);
            ratings.forEach(b::addEntity);

            b.deriveEntities(CommonTypes.USER, CommonTypes.RATING, CommonAttributes.USER_ID);
            b.deriveEntities(CommonTypes.ITEM, CommonTypes.RATING, CommonAttributes.ITEM_ID);

            DataAccessObject dao = b.build();

            List<Rating> sorted = Entities.idOrdering().sortedCopy(ratings);
            List<Rating> built = dao.query(Rating.class)
                                    .orderBy(CommonAttributes.ENTITY_ID)
                                    .get();
            assertThat(built, hasSize(ratings.size()));
            assertThat(built, equalTo(sorted));

            Set<Long> userIds = ratings.stream().map(Rating::getUserId).collect(Collectors.toSet());
            Set<Long> itemIds = ratings.stream().map(Rating::getItemId).collect(Collectors.toSet());
            assertThat(dao.getEntityIds(CommonTypes.USER),
                       equalTo(userIds));
            assertThat(dao.getEntityIds(CommonTypes.ITEM),
                       equalTo(itemIds));

            for (long user: someFixedValues(userIds)) {
                 List<Rating> fromData = ratings.stream()
                                                .filter(r -> r.getUserId() == user)
                                                .sorted(Ratings.TIMESTAMP_COMPARATOR)
                                                .collect(Collectors.toList());
                 List<Rating> fromDAO = dao.query(Rating.class)
                                           .withAttribute(CommonAttributes.USER_ID, user)
                                           .orderBy(CommonAttributes.TIMESTAMP)
                                           .get();
                 assertThat(fromDAO, equalTo(fromData));
            }

            for (long item: someFixedValues(itemIds)) {
                List<Rating> fromData = ratings.stream()
                                               .filter(r -> r.getItemId() == item)
                                               .sorted(Ratings.TIMESTAMP_COMPARATOR)
                                               .collect(Collectors.toList());
                List<Rating> fromDAO = dao.query(Rating.class)
                                          .withAttribute(CommonAttributes.ITEM_ID, item)
                                          .orderBy(CommonAttributes.TIMESTAMP)
                                          .get();
                assertThat(fromDAO, equalTo(fromData));
            }
        }
    }
}

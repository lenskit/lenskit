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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.dao.EntityCollectionDAO;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.store.EntityCollection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class StaticDataSourceTest {
    private EntityFactory factory = new EntityFactory();
    private ObjectReader reader = new ObjectMapper().reader();

    @Test
    public void testSomeEvents() {
        StaticDataSource layout = new StaticDataSource();
        List<Entity> ratings = Lists.<Entity>newArrayList(factory.rating(1L, 20L, 3.5),
                                                          factory.rating(1L, 21L, 4.5));
        layout.addSource(ratings);

        // we should have one collection source for entities
        assertThat(layout.getSourcesForType(CommonTypes.RATING),
                   contains(instanceOf(CollectionEntitySource.class)));

        DataAccessObject dao = layout.get();
        assertThat(dao.getEntityTypes(), containsInAnyOrder(CommonTypes.RATING,
                                                            CommonTypes.USER,
                                                            CommonTypes.ITEM));
        assertThat(dao.lookupEntity(CommonTypes.RATING, ratings.get(0).getId()),
                   equalTo(ratings.get(0)));
        assertThat(dao.query(CommonTypes.RATING)
                      .withAttribute(CommonAttributes.ITEM_ID, 20L)
                      .get(),
                   contains(ratings.get(0)));

        assertThat(dao.query(CommonTypes.RATING)
                      .withAttribute(CommonAttributes.USER_ID, 1L)
                      .get(),
                   contains(ratings.toArray()));

        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(1L));
        assertThat(dao.getEntityIds(CommonTypes.ITEM),
                   containsInAnyOrder(20L, 21L));
        assertThat(dao.query(CommonTypes.USER).get(),
                   contains(Entities.create(CommonTypes.USER, 1)));
        assertThat(dao.query(CommonTypes.ITEM).get(),
                   containsInAnyOrder(Entities.create(CommonTypes.ITEM, 20),
                                      Entities.create(CommonTypes.ITEM, 21)));
    }

    @Test
    public void testIndexedEvents() {
        StaticDataSource layout = new StaticDataSource();
        layout.addIndex(CommonTypes.RATING, CommonAttributes.USER_ID);
        List<Entity> ratings = Lists.<Entity>newArrayList(factory.rating(1L, 20L, 3.5),
                                                          factory.rating(1L, 21L, 4.5));
        layout.addSource(ratings);
        DataAccessObject dao = layout.get();
        assertThat(dao.getEntityTypes(), containsInAnyOrder(CommonTypes.RATING,
                                                            CommonTypes.USER,
                                                            CommonTypes.ITEM));
        assertThat(dao.lookupEntity(CommonTypes.RATING, ratings.get(0).getId()),
                   equalTo(ratings.get(0)));
        assertThat(dao.query(CommonTypes.RATING)
                      .withAttribute(CommonAttributes.ITEM_ID, 20L)
                      .get(),
                   contains(ratings.get(0)));

        assertThat(dao.query(CommonTypes.RATING)
                      .withAttribute(CommonAttributes.USER_ID, 1L)
                      .get(),
                   contains(ratings.toArray()));

        assertThat(dao.getEntityIds(CommonTypes.USER),
                   contains(1L));
        assertThat(dao.getEntityIds(CommonTypes.ITEM),
                   containsInAnyOrder(20L, 21L));
        assertThat(dao.query(CommonTypes.USER).get(),
                   contains(Entities.create(CommonTypes.USER, 1)));
        assertThat(dao.query(CommonTypes.ITEM).get(),
                   containsInAnyOrder(Entities.create(CommonTypes.ITEM, 20),
                                      Entities.create(CommonTypes.ITEM, 21)));
    }

    @Test
    public void testLoadRatings() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("{\"file\": \"ratings.csv\", \"format\": \"csv\"}");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        // we should have one text source for ratings
        assertThat(daoProvider.getSourcesForType(CommonTypes.RATING),
                   contains(instanceOf(TextEntitySource.class)));

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao);
    }

    @Test
    public void testLoadRatingsList() throws IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("[{\"file\": \"ratings.csv\", \"format\": \"csv\"}]");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao);

        Field f = FieldUtils.getField(EntityCollectionDAO.class, "storage", true);
        Class<?> cls = Class.forName("org.lenskit.data.store.PackedEntityCollection");
        Map<EntityType,EntityCollection> storage = (Map<EntityType, EntityCollection>) f.get(dao);
        assertThat(storage.get(CommonTypes.RATING),
                   instanceOf(cls));
    }

    @Test
    public void testLoadRatingsMap() throws IOException, URISyntaxException, ClassNotFoundException, IllegalAccessException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("{\"ratings\":{\"file\": \"ratings.csv\", \"format\": \"csv\"}}");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao);

        Field f = FieldUtils.getField(EntityCollectionDAO.class, "storage", true);
        Class<?> cls = Class.forName("org.lenskit.data.store.PackedEntityCollection");
        Map<EntityType,EntityCollection> storage = (Map<EntityType, EntityCollection>) f.get(dao);
        assertThat(storage.get(CommonTypes.RATING),
                   instanceOf(cls));
    }

    @Test
    public void testLoadRatingsDeriveBobcats() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("[{\"file\": \"ratings.csv\", \"format\": \"csv\"}, {\"type\": \"derived\", \"source_type\": \"rating\", \"entity_type\": \"bobcat\", \"source_attribute\": \"item\"}]");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        // we should have one text source for ratings; derived aren't sources
        assertThat(daoProvider.getSourcesForType(CommonTypes.RATING),
                   contains(instanceOf(TextEntitySource.class)));

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao, EntityType.forName("bobcat"));

        // we should have have a bunch of bobcats
        LongSet bobcats = dao.getEntityIds(EntityType.forName("bobcat"));
        assertThat(bobcats, equalTo(dao.getEntityIds(CommonTypes.ITEM)));
    }

    @Test
    public void testLoadInvalidDataSource() throws URISyntaxException, IOException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("\"foobar\"");
        try {
            StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);
            fail("JSON parsing succeeded, should have failed on string");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    private void verifyRatingsCsvData(DataAccessObject dao, EntityType... extraTypes) {
        EntityType[] ets = new EntityType[3 + extraTypes.length];
        ets[0] = CommonTypes.RATING;
        ets[1] = CommonTypes.USER;
        ets[2] = CommonTypes.ITEM;
        System.arraycopy(extraTypes, 0, ets, 3, extraTypes.length);
        assertThat(dao.getEntityTypes(), containsInAnyOrder(ets));

        List<Entity> ratings = dao.query(CommonTypes.RATING).get();
        assertThat(ratings, hasSize(2));
        // turn this off because packed loading violates!
        // assertThat(ratings, (Matcher) everyItem(instanceOf(Rating.class)));

        Entity first = ratings.get(0);
        assertThat(first.getType(), equalTo(EntityType.forName("rating")));
        assertThat(first.getId(), equalTo(1L));
        assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(1L));
        assertThat(first.get(CommonAttributes.ITEM_ID), equalTo(20L));
        assertThat(first.get(CommonAttributes.USER_ID), equalTo(10L));
        assertThat(first.get(CommonAttributes.RATING), equalTo(3.5));
        assertThat(first.hasAttribute(CommonAttributes.TIMESTAMP),
                   equalTo(false));

        Entity second = ratings.get(1);
        assertThat(second.getType(), equalTo(EntityType.forName("rating")));
        assertThat(second.getId(), equalTo(2L));
        assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(2L));
        assertThat(second.get(CommonAttributes.ITEM_ID), equalTo(20L));
        assertThat(second.get(CommonAttributes.USER_ID), equalTo(11L));
        assertThat(second.get(CommonAttributes.RATING), equalTo(4.0));
        assertThat(second.hasAttribute(CommonAttributes.TIMESTAMP),
                   equalTo(false));

        // we have two users
        assertThat(dao.query(CommonTypes.USER).get(),
                   containsInAnyOrder(Entities.create(CommonTypes.USER, 10),
                                      Entities.create(CommonTypes.USER, 11)));

        // and one item
        assertThat(dao.query(CommonTypes.ITEM).get(),
                   contains(Entities.create(CommonTypes.ITEM, 20)));

        // check a view query
        List<Rating> rlist = dao.query(Rating.class)
                                .get();
        assertThat(rlist, hasSize(2));
        assertThat(rlist, (Matcher) equalTo(ratings));
    }
}

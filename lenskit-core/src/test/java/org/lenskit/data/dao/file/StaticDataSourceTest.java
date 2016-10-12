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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
    public void testLoadRatingsList() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("[{\"file\": \"ratings.csv\", \"format\": \"csv\"}]");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao);
    }

    @Test
    public void testLoadRatingsMap() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("ratings.csv").toURI();
        JsonNode node = reader.readTree("{\"ratings\":{\"file\": \"ratings.csv\", \"format\": \"csv\"}}");
        StaticDataSource daoProvider = StaticDataSource.fromJSON(node, baseURI);

        DataAccessObject dao = daoProvider.get();
        verifyRatingsCsvData(dao);
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

    private void verifyRatingsCsvData(DataAccessObject dao) {
        assertThat(dao.getEntityTypes(), containsInAnyOrder(CommonTypes.RATING,
                                                            CommonTypes.USER,
                                                            CommonTypes.ITEM));

        List<Entity> ratings = dao.query(CommonTypes.RATING).get();
        assertThat(ratings, hasSize(2));
        assertThat(ratings, (Matcher) everyItem(instanceOf(Rating.class)));

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

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
import org.grouplens.grapht.util.ClassLoaders;
import org.junit.Test;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.entities.Entities;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JSONEntityFormatTest {
    @Test
    public void testBareEntity() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.USER);

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"$id\": 203810}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(203810L));
        assertThat(res.getType(), equalTo(CommonTypes.USER));
        assertThat(res, equalTo(Entities.create(CommonTypes.USER, 203810)));
    }

    @Test
    public void testRating() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.RATING);
        fmt.setEntityBuilder(RatingBuilder.class);

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"$id\": 203810, \"user\": 42, \"item\": 20, \"rating\": 3.5}");
        assertThat(res, notNullValue());
        assertThat(res, instanceOf(Rating.class));
        Rating r = (Rating) res;
        assertThat(r.getId(), equalTo(203810L));
        assertThat(r.getType(), equalTo(CommonTypes.RATING));
        assertThat(r.getUserId(), equalTo(42L));
        assertThat(r.getItemId(), equalTo(20L));
        assertThat(r.getValue(), equalTo(3.5));
    }

    @Test
    public void testThingFields() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.ITEM);
        fmt.addAttribute(CommonAttributes.ENTITY_ID);
        fmt.addAttribute("title", CommonAttributes.NAME);

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"id\": 204, \"title\": \"hamster\", \"extra\": \"wumpus\"}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(204L));
        assertThat(res.get(CommonAttributes.NAME), equalTo("hamster"));
        assertThat(res.hasAttribute("extra"), equalTo(false));
    }

    @Test
    public void testConfigureReader() throws IOException {
        ObjectReader reader = new ObjectMapper().reader();
        JsonNode json = reader.readTree("{\"entity_type\": \"item\"}");
        EntityFormat fmt = JSONEntityFormat.fromJSON(null, ClassLoaders.inferDefault(), json);
        assertThat(fmt.getEntityType(), equalTo(CommonTypes.ITEM));

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"id\": 204, \"name\": \"hamster\", \"extra\": \"wumpus\"}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(204L));
        assertThat(res.get(CommonAttributes.NAME), equalTo("hamster"));
        assertThat(res.get("extra"), equalTo("wumpus"));
    }

    @Test
    public void testConfigureReaderFields() throws IOException {
        ObjectReader reader = new ObjectMapper().reader();
        JsonNode json = reader.readTree("{\"entity_type\": \"item\", \"attributes\": {\"id\": \"long\", \"title\": {\"name\": \"name\", \"type\": \"string\"}}}");
        JSONEntityFormat fmt = JSONEntityFormat.fromJSON(null, ClassLoaders.inferDefault(), json);
        assertThat(fmt.getEntityType(), equalTo(CommonTypes.ITEM));
        assertThat(fmt.getAttributes(), hasEntry("id", CommonAttributes.ENTITY_ID));
        assertThat(fmt.getAttributes(), hasEntry("title", CommonAttributes.NAME));
        assertThat(fmt.getAttributes().size(), equalTo(2));

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"id\": 204, \"title\": \"hamster\", \"extra\": \"wumpus\"}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(204L));
        assertThat(res.get(CommonAttributes.NAME), equalTo("hamster"));
        assertThat(res.hasAttribute("extra"), equalTo(false));
    }
}
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
import com.google.common.reflect.TypeToken;
import groovyjarjarantlr.CommonASTWithHiddenTokens;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.util.TypeUtils;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class JSONEntityFormatTest {
    @Test
    public void testBareEntity() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.USER);

        assertThat(fmt.getAttributes(), nullValue());

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
    public void testRatingWithNull() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.RATING);
        fmt.setEntityBuilder(RatingBuilder.class);

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"$id\": 203810, \"user\": 42, \"item\": 20, \"rating\": 3.5, \"timestamp\": null}");
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
    public void testThingNullField() {
        JSONEntityFormat fmt = new JSONEntityFormat();
        fmt.setEntityType(CommonTypes.ITEM);
        fmt.addAttribute(CommonAttributes.ENTITY_ID);
        fmt.addAttribute("title", CommonAttributes.NAME);

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"id\": 204, \"title\": null, \"extra\": \"wumpus\"}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(204L));
        assertThat(res.maybeGet(CommonAttributes.NAME), nullValue());
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
        assertThat(res.get("extra"), (Matcher) equalTo("wumpus"));
    }

    @Test
    public void testConfigureReaderCompoundField() throws IOException {
        TypeToken<List<String>> sl = TypeUtils.makeListType(TypeToken.of(String.class));
        TypedName<List<String>> tlName = TypedName.create("tags", sl);

        ObjectReader reader = new ObjectMapper().reader();
        JsonNode json = reader.readTree("{\"entity_type\": \"item\", \"attributes\": {\"id\": \"long\", \"title\": {\"name\": \"name\", \"type\": \"string\"}, \"tags\": \"string[]\"}}");
        JSONEntityFormat fmt = JSONEntityFormat.fromJSON(null, ClassLoaders.inferDefault(), json);
        assertThat(fmt.getEntityType(), equalTo(CommonTypes.ITEM));
        assertThat(fmt.getDefinedAttributes(), hasEntry("id", (TypedName) CommonAttributes.ENTITY_ID));
        assertThat(fmt.getDefinedAttributes(), hasEntry("title", (TypedName) CommonAttributes.NAME));
        assertThat(fmt.getDefinedAttributes(), hasEntry("tags", (TypedName) tlName));
        assertThat(fmt.getDefinedAttributes().size(), equalTo(3));

        assertThat(fmt.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID, CommonAttributes.NAME, tlName));

        LineEntityParser lep = fmt.makeParser(Collections.EMPTY_LIST);
        Entity res = lep.parse("{\"id\": 204, \"title\": \"hamster\", \"tags\": [\"foo\", \"bar\"]}");
        assertThat(res, notNullValue());
        assertThat(res.getId(), equalTo(204L));
        assertThat(res.get(CommonAttributes.NAME), equalTo("hamster"));
        assertThat(res.get(tlName), contains("foo", "bar"));
    }
}
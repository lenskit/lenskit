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
import org.junit.Test;
import org.lenskit.data.entities.*;
import org.lenskit.data.ratings.Rating;
import org.lenskit.data.ratings.RatingBuilder;
import org.lenskit.util.io.ObjectStream;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TextEntitySourceTest {
    private ObjectReader reader = new ObjectMapper().reader();

    @Test
    public void testMinimalTSVConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"name\": \"woozle\"}");
        EntitySource raw = EntitySources.fromJSON(node, Paths.get("").toUri());
        assertThat(raw, notNullValue());
        assertThat(raw, instanceOf(TextEntitySource.class));
        TextEntitySource src = (TextEntitySource) raw;
        assertThat(src.getName(), equalTo("woozle"));
        assertThat(src.getURL(), equalTo(Paths.get("ratings.tsv").toUri().toURL()));
        assertThat(src.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) src.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));

        EntitySource.Layout layout = src.getLayout();
        assertThat(layout, notNullValue());
        assertThat(layout.getEntityType(), equalTo(CommonTypes.RATING));
        assertThat(layout.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.USER_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.RATING,
                                      CommonAttributes.TIMESTAMP));
    }

    @Test
    public void testMinimalCSVConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.csv\", \"format\": \"csv\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get("").toUri());
        assertThat(fr, notNullValue());
        assertThat(fr.getURL(), equalTo(Paths.get("ratings.csv").toUri().toURL()));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo(","));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        assertThat(format.getBaseId(), equalTo(0L));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testWeirdDelimiterConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.dat\", \"format\": \"delimited\", \"delimiter\": \"::\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get("").toUri());
        assertThat(fr, notNullValue());
        assertThat(fr.getURL(), equalTo(Paths.get("ratings.dat").toUri().toURL()));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("::"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testHeaderConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"header\": true}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get("").toUri());
        assertThat(fr, notNullValue());
        assertThat(fr.getURL(), equalTo(Paths.get("ratings.tsv").toUri().toURL()));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(1));
        assertThat(format.usesHeader(), equalTo(true));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testSkipHeaderConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"header\": 2}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get("").toUri());
        assertThat(fr, notNullValue());
        assertThat(fr.getURL(), equalTo(Paths.get("ratings.tsv").toUri().toURL()));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(2));
        assertThat(format.usesHeader(), equalTo(false));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testBaseIdConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"base_id\": 100}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get("").toUri());
        assertThat(fr, notNullValue());
        assertThat(fr.getURL(), equalTo(Paths.get("ratings.tsv").toUri().toURL()));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getBaseId(), equalTo(100L));
        assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testJSONConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.json\", \"name\": \"woozle\", \"format\": \"json\", \"entity_type\": \"item\"}");
        EntitySource raw = EntitySources.fromJSON(node, Paths.get("").toUri());
        assertThat(raw, notNullValue());
        assertThat(raw, instanceOf(TextEntitySource.class));
        TextEntitySource src = (TextEntitySource) raw;
        assertThat(src.getName(), equalTo("woozle"));
        assertThat(src.getURL(), equalTo(Paths.get("ratings.json").toUri().toURL()));
        assertThat(src.getFormat(), instanceOf(JSONEntityFormat.class));
        JSONEntityFormat format = (JSONEntityFormat) src.getFormat();
        assertThat(format.getEntityType(), equalTo(CommonTypes.ITEM));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.getEntityBuilder(), equalTo((Class) BasicEntityBuilder.class));

        assertThat(src.getLayout(),
                   nullValue());
    }

    @Test
    public void testReadRatingCSV() throws IOException {
        TextEntitySource fr = new TextEntitySource();
        fr.setSource("10,20,3.5\n11,20,4.0\n");
        fr.setFormat(Formats.csvRatings());

        try (ObjectStream<Entity> stream = fr.openStream()) {
            Entity first = stream.readObject();
            assertThat(first, instanceOf(Rating.class));
            assertThat(first.getType(), equalTo(EntityType.forName("rating")));
            assertThat(first.getId(), equalTo(1L));
            assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(1L));
            assertThat(first.get(CommonAttributes.USER_ID), equalTo(10L));
            assertThat(first.get(CommonAttributes.ITEM_ID), equalTo(20L));
            assertThat(first.get(CommonAttributes.RATING), equalTo(3.5));
            assertThat(first.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            Entity second = stream.readObject();
            assertThat(second, instanceOf(Rating.class));
            assertThat(second.getType(), equalTo(EntityType.forName("rating")));
            assertThat(second.getId(), equalTo(2L));
            assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(2L));
            assertThat(second.get(CommonAttributes.USER_ID), equalTo(11L));
            assertThat(second.get(CommonAttributes.ITEM_ID), equalTo(20L));
            assertThat(second.get(CommonAttributes.RATING), equalTo(4.0));
            assertThat(second.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));
            assertThat(((Rating) second).getUserId(),
                       equalTo(11L));
            assertThat(((Rating) second).getItemId(),
                       equalTo(20L));
            assertThat(((Rating) second).getValue(),
                       equalTo(4.0));

            assertThat(stream.readObject(), nullValue());
        }
    }

    @Test
    public void testReadRatingCSVWithHeaders() throws IOException {
        TextEntitySource fr = new TextEntitySource();
        fr.setSource("id,item,user,rating\n101,10,20,3.5\n109,11,20,4.0\n");
        DelimitedColumnEntityFormat fmt = new DelimitedColumnEntityFormat();
        fmt.setDelimiter(",");
        fmt.setEntityType(CommonTypes.RATING);
        fmt.setEntityBuilder(BasicEntityBuilder.class);
        fmt.setHeader(true);
        fmt.addColumn("user", CommonAttributes.USER_ID);
        fmt.addColumn("item", CommonAttributes.ITEM_ID);
        fmt.addColumn("rating", CommonAttributes.RATING);
        fmt.addColumn("id", CommonAttributes.ENTITY_ID);
        fr.setFormat(fmt);

        try (ObjectStream<Entity> stream = fr.openStream()) {
            Entity first = stream.readObject();
            assertThat(first.getType(), equalTo(EntityType.forName("rating")));
            assertThat(first.getId(), equalTo(101L));
            assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(101L));
            assertThat(first.get(CommonAttributes.ITEM_ID), equalTo(10L));
            assertThat(first.get(CommonAttributes.USER_ID), equalTo(20L));
            assertThat(first.get(CommonAttributes.RATING), equalTo(3.5));
            assertThat(first.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            Entity second = stream.readObject();
            assertThat(second.getType(), equalTo(EntityType.forName("rating")));
            assertThat(second.getId(), equalTo(109L));
            assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(109L));
            assertThat(second.get(CommonAttributes.ITEM_ID), equalTo(11L));
            assertThat(second.get(CommonAttributes.USER_ID), equalTo(20L));
            assertThat(second.get(CommonAttributes.RATING), equalTo(4.0));
            assertThat(second.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            assertThat(stream.readObject(), nullValue());
        }
    }

    @Test
    public void testConfigureRatingCSVWithHeaders() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("header-ratings.csv").toURI();
        JsonNode node = reader.readTree("{\"file\": \"header-ratings.csv\", \"header\": true, \"format\": \"csv\",\n" +
                                                "\"columns\": {\n" +
                                                "  \"id\": \"id\",\n" +
                                                "  \"movie\": \"item\",\n" +
                                                "  \"user\": \"user\",\n" +
                                                "  \"rating\": \"rating\"\n" +
                                                "}}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, baseURI);
        EntitySource.Layout layout = fr.getLayout();
        assertThat(layout, notNullValue());
        assertThat(layout.getEntityType(), equalTo(CommonTypes.RATING));
        assertThat(layout.getAttributes(),
                   containsInAnyOrder(CommonAttributes.ENTITY_ID,
                                      CommonAttributes.USER_ID,
                                      CommonAttributes.ITEM_ID,
                                      CommonAttributes.RATING));

        try (ObjectStream<Entity> stream = fr.openStream()) {
            Entity first = stream.readObject();
            assertThat(first, instanceOf(Rating.class));
            assertThat(first.getType(), equalTo(EntityType.forName("rating")));
            assertThat(first.getId(), equalTo(101L));
            assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(101L));
            assertThat(first.get(CommonAttributes.ITEM_ID), equalTo(10L));
            assertThat(first.get(CommonAttributes.USER_ID), equalTo(20L));
            assertThat(first.get(CommonAttributes.RATING), equalTo(3.5));
            assertThat(first.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            Entity second = stream.readObject();
            assertThat(second, instanceOf(Rating.class));
            assertThat(second.getType(), equalTo(EntityType.forName("rating")));
            assertThat(second.getId(), equalTo(109L));
            assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(109L));
            assertThat(second.get(CommonAttributes.ITEM_ID), equalTo(11L));
            assertThat(second.get(CommonAttributes.USER_ID), equalTo(20L));
            assertThat(second.get(CommonAttributes.RATING), equalTo(4.0));
            assertThat(second.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            assertThat(stream.readObject(), nullValue());
        }
    }
    @Test
    public void testConfigureItemJSON() throws IOException, URISyntaxException {
        URI baseURI = TextEntitySourceTest.class.getResource("header-ratings.csv").toURI();
        JsonNode node = reader.readTree("{\"file\": \"items.json\", \"format\": \"json\", \"entity_type\": \"item\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, baseURI);

        try (ObjectStream<Entity> stream = fr.openStream()) {
            Entity first = stream.readObject();
            assertThat(first.getType(), equalTo(CommonTypes.ITEM));
            assertThat(first.getId(), equalTo(42L));
            assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(42L));
            assertThat(first.get(CommonAttributes.NAME), equalTo("woozle"));

            Entity second = stream.readObject();
            assertThat(second.getType(), equalTo(CommonTypes.ITEM));
            assertThat(second.getId(), equalTo(37L));
            assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(37L));
            assertThat(second.get(CommonAttributes.NAME), equalTo("heffalump"));

            assertThat(stream.readObject(), nullValue());
        }
    }

}

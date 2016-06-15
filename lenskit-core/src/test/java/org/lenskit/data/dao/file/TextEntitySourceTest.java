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
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.Entity;
import org.lenskit.data.entities.EntityType;
import org.lenskit.util.io.ObjectStream;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TextEntitySourceTest {
    private ObjectReader reader = new ObjectMapper().reader();;

    @Test
    public void testMinimalJSONConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get(""));
        assertThat(fr, notNullValue());
        assertThat(fr.getFile(), equalTo(Paths.get("ratings.tsv")));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        // FIXME Enable this test when rating builders work
        // assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testMinimalCSVConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.csv\", \"format\": \"csv\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get(""));
        assertThat(fr, notNullValue());
        assertThat(fr.getFile(), equalTo(Paths.get("ratings.csv")));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo(","));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        // FIXME Enable this test when rating builders work
        // assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testWeirdDelimiterConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.dat\", \"format\": \"delimited\", \"delimiter\": \"::\"}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get(""));
        assertThat(fr, notNullValue());
        assertThat(fr.getFile(), equalTo(Paths.get("ratings.dat")));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("::"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(0));
        assertThat(format.usesHeader(), equalTo(false));
        // FIXME Enable this test when rating builders work
        // assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testHeaderConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"header\": true}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get(""));
        assertThat(fr, notNullValue());
        assertThat(fr.getFile(), equalTo(Paths.get("ratings.tsv")));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(1));
        assertThat(format.usesHeader(), equalTo(true));
        // FIXME Enable this test when rating builders work
        // assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testSkipHeaderConfig() throws IOException {
        JsonNode node = reader.readTree("{\"file\": \"ratings.tsv\", \"header\": 2}");
        TextEntitySource fr = TextEntitySource.fromJSON("test", node, Paths.get(""));
        assertThat(fr, notNullValue());
        assertThat(fr.getFile(), equalTo(Paths.get("ratings.tsv")));
        assertThat(fr.getFormat(), instanceOf(DelimitedColumnEntityFormat.class));
        DelimitedColumnEntityFormat format = (DelimitedColumnEntityFormat) fr.getFormat();
        assertThat(format.getDelimiter(), equalTo("\t"));
        assertThat(format.getEntityType(), equalTo(EntityType.forName("rating")));
        assertThat(format.getHeaderLines(), equalTo(2));
        assertThat(format.usesHeader(), equalTo(false));
        // FIXME Enable this test when rating builders work
        // assertThat(format.getEntityBuilder(), equalTo((Class) RatingBuilder.class));
    }

    @Test
    public void testReadRatingCSV() throws IOException {
        TextEntitySource fr = new TextEntitySource();
        fr.setSource("10,20,3.5\n11,20,4.0\n");
        fr.setFormat(Formats.csvRatings());

        try (ObjectStream<Entity> stream = fr.openStream()) {
            Entity first = stream.readObject();
            assertThat(first.getType(), equalTo(EntityType.forName("rating")));
            assertThat(first.getId(), equalTo(1L));
            assertThat(first.get(CommonAttributes.ENTITY_ID), equalTo(1L));
            assertThat(first.get(CommonAttributes.USER_ID), equalTo(10L));
            assertThat(first.get(CommonAttributes.ITEM_ID), equalTo(20L));
            assertThat(first.get(CommonAttributes.RATING), equalTo(3.5));
            assertThat(first.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            Entity second = stream.readObject();
            assertThat(second.getType(), equalTo(EntityType.forName("rating")));
            assertThat(second.getId(), equalTo(2L));
            assertThat(second.get(CommonAttributes.ENTITY_ID), equalTo(2L));
            assertThat(second.get(CommonAttributes.USER_ID), equalTo(11L));
            assertThat(second.get(CommonAttributes.ITEM_ID), equalTo(20L));
            assertThat(second.get(CommonAttributes.RATING), equalTo(4.0));
            assertThat(second.hasAttribute(CommonAttributes.TIMESTAMP),
                       equalTo(false));

            assertThat(stream.readObject(), nullValue());
        }
    }
}

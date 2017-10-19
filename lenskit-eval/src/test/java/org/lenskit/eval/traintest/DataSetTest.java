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
package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.lenskit.data.entities.CommonTypes.*;

public class DataSetTest {
    private ObjectReader reader = new ObjectMapper().reader();

    @Test
    public void testBasicConfig() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"ut\",\n" +
                                                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                                                "\"test\": {\"file\": \"test-ratings.csv\"}\n" +
                                                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);
        assertThat(dsList, hasSize(1));

        DataSet ds = dsList.get(0);
        assertThat(ds.getName(), equalTo("ut"));
        assertThat(ds.getTrainingData().getName(), equalTo("ut.train"));
        assertThat(ds.getTestData().getName(), equalTo("ut.test"));
        assertThat(ds.getRuntimeData(), nullValue());
        assertThat(ds.getEntityTypes(),
                   containsInAnyOrder(RATING));
    }

    @Test
    public void testDataSetPartitions() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"ut\",\n" +
                                                "\"datasets\": [\n" +
                                                "  { \"train\": {\"file\": \"train-ratings-1.csv\"},\n" +
                                                "    \"test\": {\"file\": \"test-ratings-1.csv\"} },\n" +
                                                "  { \"train\": {\"file\": \"train-ratings-2.csv\"},\n" +
                                                "    \"test\": {\"file\": \"test-ratings-2.csv\"} }\n" +
                                                "]}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);
        assertThat(dsList, hasSize(2));

        DataSet ds = dsList.get(0);
        assertThat(ds.getName(), equalTo("ut[1]"));
        assertThat(ds.getAttributes(), hasEntry("DataSet", (Object) "ut"));
        assertThat(ds.getAttributes(), hasEntry("Partition", (Object) 1));
        assertThat(ds.getTrainingData().getName(), equalTo("ut[1].train"));
        assertThat(ds.getTestData().getName(), equalTo("ut[1].test"));
        assertThat(ds.getRuntimeData(), nullValue());
        assertThat(ds.getEntityTypes(),
                   containsInAnyOrder(RATING));

        ds = dsList.get(1);
        assertThat(ds.getName(), equalTo("ut[2]"));
        assertThat(ds.getAttributes(), hasEntry("DataSet", (Object) "ut"));
        assertThat(ds.getAttributes(), hasEntry("Partition", (Object) 2));
        assertThat(ds.getTrainingData().getName(), equalTo("ut[2].train"));
        assertThat(ds.getTestData().getName(), equalTo("ut[2].test"));
        assertThat(ds.getRuntimeData(), nullValue());
        assertThat(ds.getEntityTypes(),
                   containsInAnyOrder(RATING));
    }

    @Test
    public void testEntityTypeArray() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"movie\", \n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"},\n" +
                "\"entity_types\": [\"user\", \"item\"]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(USER, ITEM));
    }

    @Test
    public void testEntityTypeString() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"movie\", \n" +
                "\"datasets\": [{\n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"},\n" +
                "\"entity_types\":  \"item\"\n" +
                "}]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(ITEM));
    }

    @Test
    public void testRuntimeData() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"ut\",\n" +
                                                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                                                "\"test\": {\"file\": \"test-ratings.csv\"},\n" +
                                                "\"runtime\": {\"file\": \"rt-ratings.csv\"}\n" +
                                                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);
        assertThat(dsList, hasSize(1));

        DataSet ds = dsList.get(0);
        assertThat(ds.getName(), equalTo("ut"));
        assertThat(ds.getTrainingData().getName(), equalTo("ut.train"));
        assertThat(ds.getTestData().getName(), equalTo("ut.test"));
        assertThat(ds.getRuntimeData(), notNullValue());
        assertThat(ds.getRuntimeData().getName(), equalTo("ut.runtime"));
        assertThat(ds.getEntityTypes(),
                   containsInAnyOrder(RATING));
    }
}
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
package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.lenskit.data.entities.CommonTypes.*;

/**
 * Created by kiranthapa on 2/9/17.
 */
public class DataSetTest {
    private ObjectReader reader = new ObjectMapper().reader();

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
    public void testEntityTypeNone() throws IOException {
        JsonNode node = reader.readTree("{\"name\": \"movie\", \n" +
                "\"datasets\": [{\n" +
                "\"train\": {\"file\": \"train-ratings.csv\"},\n" +
                "\"test\": {\"file\": \"test-ratings.csv\"}\n" +
                "}]\n" +
                "}");
        URI baseURI = Paths.get("").toUri();
        List<DataSet> dsList = DataSet.fromJSON(node, baseURI);

        DataSet ds = dsList.get(0);
        assertThat(ds.getEntityTypes(),
                containsInAnyOrder(RATING));
    }

}
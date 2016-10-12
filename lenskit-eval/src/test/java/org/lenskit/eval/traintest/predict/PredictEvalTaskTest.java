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
package org.lenskit.eval.traintest.predict;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class PredictEvalTaskTest {
    static JsonNode parse(String js) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(js);
    }

    @Test
    public void testCreateFromEmptySpec() throws IOException {
        JsonNode json = parse("{\"type\": \"predict\"}");
        PredictEvalTask task = PredictEvalTask.fromJSON(json, Paths.get(".").toUri());
        assertThat(task.getOutputFile(), nullValue());
        assertThat(task.getPredictMetrics(),
                   (Matcher) contains(PredictEvalTask.DEFAULT_METRICS));
    }

    @Test
    public void testConfigureOutputFile() throws IOException {
        JsonNode json = parse("{\"type\": \"predict\", \"output_file\": \"foo.csv\"}");
        PredictEvalTask task = PredictEvalTask.fromJSON(json, Paths.get(".").toUri());
        assertThat(task.getOutputFile(),
                   equalTo(Paths.get("foo.csv").toAbsolutePath()));
    }

    @Test
    public void testConfigureMetrics() throws IOException {
        JsonNode json = parse("{\"type\": \"predict\", \"metrics\": [\"rmse\", \"coverage\"]}");
        PredictEvalTask task = PredictEvalTask.fromJSON(json, Paths.get(".").toUri());
        assertThat(task.getPredictMetrics(),
                   contains(instanceOf(RMSEPredictMetric.class),
                            instanceOf(CoveragePredictMetric.class)));
    }
}

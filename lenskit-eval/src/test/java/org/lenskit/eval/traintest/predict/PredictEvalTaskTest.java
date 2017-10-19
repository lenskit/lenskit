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

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
package org.lenskit.eval.traintest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TrainTestExperimentSpecTest {
    @Test
    public void testOutputFiles() throws IOException {
        String json = "{\"output_file\": \"results.csv\", \"user_output_file\": \"users.csv\",\n" +
                "  \"datasets\": [],\n" +
                "  \"tasks\": [{\n" +
                "    \"type\": \"predict\",\n" +
                "    \"output_file\": \"predictions.csv\"\n" +
                "  }]\n" +
                "}";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode parsed = mapper.readTree(json);

        TrainTestExperiment exp = TrainTestExperiment.fromJSON(parsed, Paths.get("foo.txt").toUri());

        assertThat(exp.getOutputFile(),
                   equalTo(Paths.get("results.csv").toAbsolutePath()));
        assertThat(exp.getUserOutputFile(),
                   equalTo(Paths.get("users.csv").toAbsolutePath()));
        assertThat(exp.getPredictionTask().getOutputFile(),
                   equalTo(Paths.get("predictions.csv").toAbsolutePath()));
    }
}

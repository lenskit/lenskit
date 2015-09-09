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

import org.junit.Before;
import org.junit.Test;
import org.lenskit.specs.eval.PredictEvalTaskSpec;
import org.lenskit.specs.eval.TrainTestExperimentSpec;

import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class TrainTestExperimentSpecTest {
    TrainTestExperimentSpec spec;

    @Before
    public void newSpec() {
        spec = new TrainTestExperimentSpec();
    }

    @Test
    public void testNonEmpty() {
        TrainTestExperiment exp = TrainTestExperiment.fromSpec(spec);
        assertThat(exp, notNullValue());
        assertThat(exp.getOutputFile(), nullValue());
    }

    @Test
    public void testOutputFiles() {
        spec.setOutputFile(Paths.get("results.csv"));
        spec.setUserOutputFile(Paths.get("users.csv"));
        PredictEvalTaskSpec ets = new PredictEvalTaskSpec();
        ets.setOutputFile(Paths.get("predictions.csv"));
        spec.addTask(ets);
        assertThat(spec.getOutputFiles(),
                   containsInAnyOrder(Paths.get("results.csv"),
                                      Paths.get("users.csv"),
                                      Paths.get("predictions.csv")));
        TrainTestExperiment exp = TrainTestExperiment.fromSpec(spec);
        assertThat(exp.getOutputFile(),
                   equalTo(Paths.get("results.csv")));
        assertThat(exp.getUserOutputFile(),
                   equalTo(Paths.get("users.csv")));
        assertThat(exp.getPredictionTask().getOutputFile(),
                   equalTo(Paths.get("predictions.csv")));
    }
}

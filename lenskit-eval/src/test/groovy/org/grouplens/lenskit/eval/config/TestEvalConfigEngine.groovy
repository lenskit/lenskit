/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.config

import org.junit.Before
import org.junit.Test
import static org.hamcrest.Matchers.*
import static org.junit.Assert.*
import org.grouplens.lenskit.eval.EvalTask
import org.grouplens.lenskit.eval.traintest.TrainTestEvalTask

/**
 * Test the eval config engine and make sure it can actually load tests.
 * @author Michael Ekstrand
 */
class TestEvalConfigEngine {
    EvalConfigEngine engine;

    @Before
    void createEngine() {
        engine = new EvalConfigEngine()
    }

    private def script(name) {
        return new InputStreamReader(getClass().getResourceAsStream(name), "UTF-8")
    }

    @Test
    void testSingleEmptyEval() {
        List<EvalTask> evals = engine.load(script("emptyTrainTest.groovy"))
        assertThat(evals.size(), equalTo(1))
        def eval = evals.get(0)
        assertThat(eval, instanceOf(TrainTestEvalTask))
        assertTrue(eval.getJobGroups().isEmpty())
    }

    @Test
    void testDefaultImports() {
        List<EvalTask> evals = engine.load(script("import.groovy"))
        assertThat(evals.size(), equalTo(1))
    }
}

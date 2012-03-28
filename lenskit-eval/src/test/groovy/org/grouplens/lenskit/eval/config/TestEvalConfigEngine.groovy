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
import org.grouplens.lenskit.eval.EvalEnvironment

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
        EvalEnvironment env = engine.load(script("emptyTrainTest.groovy"))
        def eval = env.defaultTask
        assertThat(eval, instanceOf(TrainTestEvalTask))
        assertTrue(eval.getJobGroups().isEmpty())
        def evals = env.tasks
        assertThat(evals.size(), equalTo(1))
        assertThat(evals.get(0), equalTo(eval))
    }

    @Test
    void testDefaultImports() {
        EvalEnvironment env = engine.load(script("import.groovy"))
        assertThat(env.tasks.size(), equalTo(1))
    }

    @Test
    void testMultiTasks() {
        EvalEnvironment env = engine.load(script("multiple.groovy"))
        def eval = env.defaultTask
        assertThat(eval, instanceOf(TrainTestEvalTask))
        def evals = env.tasks
        assertThat(evals.size(), equalTo(2))
        assertTrue(evals.contains(eval));
        assertTrue(evals.containsAll(eval.dependencies))
    }
}

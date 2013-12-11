/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.parallel

import org.grouplens.grapht.graph.DAGNode
import org.junit.Before
import org.junit.Test

import java.util.concurrent.ExecutionException

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ParallelTaskGraphExecutorTest {
    TaskGraphExecutor exec

    @Before
    public void createExecutor() {
        exec = TaskGraphExecutor.create(2)
    }

    @Test(timeout = 1000l)
    public void runSingleTask() {
        def hasRun = false
        def graph = DAGNode.singleton {
            hasRun = true;
        }
        exec.execute(graph)
        assertThat(hasRun, equalTo(true))
    }

    @Test(timeout = 1000l)
    public void runDepTasks() {
        def at = 0
        def t1 = DAGNode.singleton {
            assertThat(at, equalTo(0))
            at = 1
        }
        def t2 = DAGNode.newBuilder({
            assertThat(at, equalTo(1))
            at = 2
        }).addEdge(t1, "foo").build()
        def t3 = DAGNode.newBuilder({
            assertThat(at, equalTo(2))
            at = 3
        }).addEdge(t1, "foo").addEdge(t2, "bar").build()
        def root = DAGNode.newBuilder({
            assertThat(at, equalTo(3))
            at = 4
        }).addEdge(t3, "root").build()
        exec.execute(root)
        assertThat(at, equalTo(4))
    }

    @Test(timeout = 1000l)
    public void runErrorTask() {
        def graph = DAGNode.singleton {
            throw new RuntimeException("I failed")
        }
        try {
            exec.execute(graph)
            fail "executing bad task should fail"
        } catch (ExecutionException ex) {
            assertThat(ex.cause, instanceOf(RuntimeException))
        }
    }
}

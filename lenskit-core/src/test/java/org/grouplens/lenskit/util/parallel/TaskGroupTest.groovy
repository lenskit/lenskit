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

import com.google.common.util.concurrent.Callables
import com.google.common.util.concurrent.MoreExecutors
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

import static groovy.test.GroovyAssert.shouldFail
import static org.hamcrest.Matchers.allOf
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.instanceOf
import static org.hamcrest.Matchers.sameInstance
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail;

/**
 * Test for the task group class.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TaskGroupTest {
    TaskGroup group
    ExecutorService service

    @Before
    void createTaskGroup() {
        group = new TaskGroup()
        service = Executors.newSingleThreadExecutor()
    }

    @After
    void shutdownService() {
        service.shutdown()
    }

    @Test
    void testEmptyGroup() throws ExecutionException {
        group.execute(service)
        // that should run successfully, only test we need
    }

    @Test
    void testCannotAddCallableAfterStarted() throws ExecutionException {
        group.execute(service)
        shouldFail(IllegalStateException) {
            group.addTask(Callables.returning("foo"))
        }
    }

    @Test
    void testCannotAddRunnableAfterStarted() {
        group.execute(service)
        shouldFail(IllegalStateException) {
            group.addTask((Runnable) {
                throw new AssertionError("why am I running?")
            })
        }
    }

    @Test
    void testStartAfterStarted() throws ExecutionException {
        group.execute(service)
        shouldFail(IllegalStateException) {
            group.execute(service)
        }
    }

    @Test
    void testRunATask() {
        def counter = new AtomicInteger(0)
        group.addTask {
            counter.addAndGet(1)
        }
        group.execute(service)
        assertThat counter.get(), equalTo(1)
    }

    @Test
    void testRunTwoTasks() {
        def counter = new AtomicInteger(0)
        group.addTask {
            counter.addAndGet(1)
        }
        group.addTask {
            counter.addAndGet(2)
        }
        group.execute(service)
        assertThat counter.get(), equalTo(3)
    }

    @Test
    void testCancelOnFailedTask() {
        // this test assumes that the single-thread executor service runs tasks in order
        def counter = new AtomicInteger(0)
        group.addTask {
            throw new RuntimeException("foo")
        }
        group.addTask {
            counter.addAndGet(2)
        }
        assertThat group.taskCount, equalTo(2)
        try {
            group.execute(service)
            fail "execution with failing task should fail"
        } catch (ExecutionException e) {
            assertThat e.cause, instanceOf(RuntimeException)
            assertThat e.cause.message, equalTo("foo")
        }
        assertThat counter.get(), equalTo(0)
    }

    @Test
    void testRunAllTasks() {
        def counter = new AtomicInteger(0)
        def tasks = []
        tasks << {
            counter.addAndGet(1)
        }
        tasks << {
            counter.addAndGet(2)
        }
        group.addAll(tasks)
        group.execute(service)
        assertThat counter.get(), equalTo(3)
    }

    @Test
    void testRunTwoTasksOnSameThread() {
        def counter = new AtomicInteger(0)
        Thread current = Thread.currentThread()
        group.addTask {
            assertThat(Thread.currentThread(), sameInstance(current))
            counter.addAndGet(1)
        }
        group.addTask {
            assertThat(Thread.currentThread(), sameInstance(current))
            counter.addAndGet(2)
        }
        group.execute(MoreExecutors.sameThreadExecutor())
        assertThat counter.get(), equalTo(3)
    }
}

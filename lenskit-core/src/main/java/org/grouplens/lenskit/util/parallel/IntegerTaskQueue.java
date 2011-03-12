/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.util.parallel;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A task queue that returns (long) integer task IDs.
 *
 * This task queue represents tasks as consecutive, increasing integer IDs,
 * starting with 0.  All active iterators are considered threads, and each one
 * returns the next global task ID.  This allows multiple threads to start up,
 * each one to start an iterator loop overthe task queue, and the work to get
 * done.  Updates are done atomically so the whole thing is lock-free on good
 * hardware.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IntegerTaskQueue {
    private final int taskCount;
    private AtomicInteger nextTask;

    public IntegerTaskQueue(int ntasks)
    {
        this.taskCount = ntasks;
        this.nextTask = new AtomicInteger();
    }

    public long getTaskCount() {
        return taskCount;
    }

    public <W extends IntWorker> void run(WorkerFactory<W> factory, int nthreads) {
        Queue<Thread> threads = new LinkedList<Thread>();
        ThreadGroup group = new ThreadGroup(factory.getClass().getName());
        for (int i = 0; i < nthreads; i++) {
            String name = String.format("%s(%d)", factory.getClass().getName(), i);
            Thread t = new TaskThread<W>(group, name, factory);
            threads.add(t);
            t.start();
        }
        while (!threads.isEmpty()) {
            Thread t = (Thread)threads.element();
            try {
                t.join(100);
                if (!t.isAlive())
                    threads.remove();
            }
            catch (InterruptedException localInterruptedException) {
                /* no-op */
            }
        }
    }

    private class TaskThread<W extends IntWorker> extends Thread {
        private final WorkerFactory<W> factory;
        public TaskThread(ThreadGroup group, String name, WorkerFactory<W> factory) {
            super(group, name);
            this.factory = factory;
        }

        @Override
        public void run() {
            IntWorker worker = factory.create(this);
            for (int job = nextTask.getAndIncrement(); job < taskCount;
                     job = nextTask.getAndIncrement()) {
                worker.doJob(job);
            }
            worker.finish();
        }
    }
}

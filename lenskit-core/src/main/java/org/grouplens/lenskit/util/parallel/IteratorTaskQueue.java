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
package org.grouplens.lenskit.util.parallel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class IteratorTaskQueue<I,W extends ObjectWorker<I>> {
    private final Iterator<I> iterator;
    private final WorkerFactory<W> factory;

    public IteratorTaskQueue(Iterator<I> iter, WorkerFactory<W> factory) {
        iterator = iter;
        this.factory = factory;
    }

    public void run(int nthreads) {
        Queue<Thread> threads = new LinkedList<Thread>();
        ThreadGroup group = new ThreadGroup(factory.getClass().getName());
        for (int i = 0; i < nthreads; i++) {
            String name = String.format("%s(%d)", factory.getClass().getName(), i);
            Thread t = new TaskThread(group, name);
            threads.add(t);
            t.start();
        }
        while (!threads.isEmpty()) {
            // FIXME handle exceptions in worker threads
            Thread t = threads.element();
            try {
                t.join();
                if (!t.isAlive())
                    threads.remove();
            } catch (InterruptedException e) {
                /* no-op, try again */
            }
        }
    }
    public static <I,W extends ObjectWorker<I>> void parallelDo(Iterator<I> iter, int nthreads, WorkerFactory<W> factory) {
        IteratorTaskQueue<I, W> queue = new IteratorTaskQueue<I, W>(iter, factory);
        queue.run(nthreads);
    }

    private synchronized I nextObject() {
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return null;
        }
    }

    private class TaskThread extends Thread {
        public TaskThread(ThreadGroup group, String name) {
            super(group, name);
        }
        @Override
        public void run() {
            W worker = factory.create(this);
            I item;
            while ((item = nextObject()) != null) {
                worker.doJob(item);
            }
            worker.finish();
        }

    }
}

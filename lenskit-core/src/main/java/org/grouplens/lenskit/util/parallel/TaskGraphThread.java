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
package org.grouplens.lenskit.util.parallel;

import org.grouplens.grapht.graph.DAGNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * Thread for running task graph jobs.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class TaskGraphThread<T extends Callable<?>,E> extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(TaskGraphThread.class);
    private final TaskGraphManager<T,E> manager;

    public TaskGraphThread(TaskGraphManager<T,E> mgr, String name) {
        super(name);
        manager = mgr;
    }

    public void run() {
        DAGNode<T,E> task = null;
        try {
            task = manager.getRunnableTask();
        } catch (InterruptedException e) {
            logger.debug("thread {} interrupted", getName());
            task = null;
        }
        while (task != null) {
            try {
                logger.info("executing task {}", task.getLabel());
                task.getLabel().call();
                manager.taskFinished(task, null);
            } catch (Throwable th) {
                logger.error("error in task " + task.getLabel(), th);
                manager.taskFinished(task, th);
            }
            try {
                task = manager.getRunnableTask();
            } catch (InterruptedException e) {
                logger.debug("thread {} interrupted", getName());
                task = null;
            }
        }
        manager.threadTerminating();
    }
}

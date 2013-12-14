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

import com.google.common.base.Preconditions;
import org.grouplens.grapht.graph.DAGNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Multithreaded task graph executor.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class ParallelTaskGraphExecutor extends TaskGraphExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ParallelTaskGraphExecutor.class);
    private final int threadCount;
    private final String name;

    ParallelTaskGraphExecutor(int nthreads, String n) {
        Preconditions.checkArgument(nthreads > 0, "thread count is not positive");
        threadCount = nthreads;
        name = n;
    }

    @Override
    public <T extends Callable<?>,E> void execute(DAGNode<T, E> graph) throws ExecutionException, InterruptedException {
        logger.info("{}: executing {} tasks on {} threads", name,
                    graph.getReachableNodes().size(), threadCount);
        TaskGraphManager<T,E> manager = new TaskGraphManager<T,E>(name, graph);
        for (int i = 1; i <= threadCount; i++) {
            Thread thread = new TaskGraphThread<T,E>(manager, String.format("%s-%d", name, i));
            manager.addThread(thread);
            thread.start();
        }
        manager.waitForFinished();
    }
}

/*
 * LensKit, a reference implementation of recommender algorithms.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Helper methods for working with futures and executor services.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ExecHelpers {
    /**
     * Extract the cause exception for an execution exception if possible.
     * @param e The execution exception.
     * @return The cause of <var>e</var>, if set; otherwise, <var>e</var>.
     */
    public static Throwable unwrapExecutionException(ExecutionException e) {
        if (e.getCause() != null)
            return e.getCause();
        else
            return e;
    }

    /**
     * Run a collection of tasks in an executor and wait for all to finish.
     * @param svc The executor service to use.
     * @param tasks The tasks to run.
     * @throws ExecutionException if one of the tasks failed.  If multiple tasks
     * failed, it is undefined which exception is actually thrown.
     */
    public static void parallelRun(ExecutorService svc, Collection<? extends Runnable> tasks) throws ExecutionException {
        List<Future<?>> results = new ArrayList<Future<?>>(tasks.size());
        for (Runnable task: tasks) {
            results.add(svc.submit(task));
        }
        waitAll(results);
    }

    /**
     * Wait for all futures to finish.
     * 
     * @param results The futures to wait on.
     * @throws ExecutionException if one or more futures failed. Remaining
     *         futures are not waited for.
     * @review Should we wait for all futures, then throw all the errors
     *         together?
     */
    public static void waitAll(List<Future<?>> results)
            throws ExecutionException {
        for (Future<?> f: results) {
            Uninterruptibles.getUninterruptibly(f);
        }
    }
}

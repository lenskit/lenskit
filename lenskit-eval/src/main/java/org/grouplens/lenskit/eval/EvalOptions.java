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
package org.grouplens.lenskit.eval;

/**
 * Options for evaluation tasks.
 */
public class EvalOptions {
    private boolean force = false;
    private int threadCount = 1;

    public EvalOptions() {}

    /**
     * Set force mode.
     * @param on {@code true} to turn on force mode.
     * @return The options (for chaining).
     * @see #isForce()
     */
    public EvalOptions setForce(boolean on) {
        force = on;
        return this;
    }

    /**
     * Query whether this run is in "force" mode. In force mode, tasks should do their
     * work irregardless of whether files are already up-to-date.
     * @return {@code true} if the evaluation is running in force mode.
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Get the thread count for this run.
     * @return The number of threads to use.
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * Set the number of threads for tasks to use. At present, tasks are run sequentially, so this
     * is only an instruction for how tasks are to do internal parallelism.
     * @param threadCount The number of threads to use.
     * @return The options (for chaining).
     */
    public EvalOptions setThreadCount(int threadCount) {
        this.threadCount = threadCount;
        return this;
    }
}

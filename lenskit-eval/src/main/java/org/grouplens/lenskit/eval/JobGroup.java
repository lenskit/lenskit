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
package org.grouplens.lenskit.eval;

import java.util.List;

/**
 * A group of jobs for an evaluation.  Jobs should be grouped by some major
 * resource they share, as appropate; a typical evaluator will group jobs by
 * data set.  The jobs in each group will be the set of evaluations to run on
 * that data set.
 *
 * <p>When the evaluation is being run in isolate mode, each job group will
 * be run in its entirety before the next group is run; this allows for large
 * data structures such as in-memory caches to be shared between jobs, with only
 * one such set in memory at a time.
 *
 * @param <T> The return type of jobs in this group.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public interface JobGroup<T> {
    /**
     * Get the name of this job group. This group will be displayed in the UI,
     * and used to allow the user to selectively enable certain job groups when
     * runninv evaluations.
     *
     * @return The name of the job group.
     */
    String getName();

    /**
     * Start the job group. This is called before any jobs in the group are run
     * and before any child groups are started. Job groups can pre-execute data
     * here, but they are encouraged to do such loads lazily.
     */
    void start();

    /**
     * Finish the job group.  This is called after the last job in the group
     * has been run.  Any resources held by the job group should be freed here.
     * All child groups will have been finished before this method is called.
     *
     * <p>Executors will try to call this method when the execution has failed
     * as well, although that is on a best-effort basis.  Termination without
     * calling this method shouldn't leave external resources in horribly broken
     * states.
     */
    void finish();

    /**
     * Get the jobs in this job group.
     *
     * @return The list of jobs to run in this group.
     */
    List<Job<T>> getJobs();
}

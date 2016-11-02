/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.monitor;

import org.slf4j.Logger;

/**
 * Component for tracking the status of jobs.  Unlike {@link org.lenskit.util.ProgressLogger}, this is for large jobs
 * that should have each completion reported.
 */
public class StatusTracker {
    private final Logger logger;
    private int jobCount = 0;
    private int failed = 0;
    private int finished = 0;

    /**
     * Construct a new status tracker.
     * @param log The logger to report statuses to.
     */
    public StatusTracker(Logger log) {
        logger = log;
    }

    /**
     * Add a job to the status tracker.
     * @param job The job to track.
     */
    public void addJob(Object job) {
        jobCount += 1;
    }

    public void reportFailure(Object job, Throwable th) {
        String msg = String.format("job %d of %d (%s) failed: %s",
                                   finished + failed + 1, jobCount, job, th);
        logger.error(msg);
        failed += 1;
    }

    public void reportSuccess(Object job) {
        String msg = String.format("job %d of %d (%s) finished", finished + failed + 1, jobCount, job);
        finished += 1;
        logger.info(msg);
    }
}

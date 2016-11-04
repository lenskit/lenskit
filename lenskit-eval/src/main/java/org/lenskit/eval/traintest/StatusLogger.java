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
package org.lenskit.eval.traintest;

import com.google.common.eventbus.Subscribe;
import org.lenskit.util.monitor.JobEvent;
import org.lenskit.util.monitor.TrackedJob;
import org.slf4j.Logger;

/**
 * Event listener that logs status messages.
 */
public class StatusLogger {
    private final Logger logger;

    /**
     * Construct a new status logger writing to a logger.
     * @param log The logger.
     */
    public StatusLogger(Logger log) {
        logger = log;
    }

    @Subscribe
    public void jobStarted(JobEvent.Started js) {
        TrackedJob job = js.getJob();
        TrackedJob parent = job.getParent();

        if (job.getType().equals(ExperimentJob.JOB_TYPE)) {
            assert parent != null;
            logger.info("started eval job {} of {}: {}",
                        parent.getChildrenFinished() + parent.getChildrenRunning(),
                        parent.getChildCount(), job.getDescription());
        } else if (parent != null && parent.getType().equals(ExperimentJob.JOB_TYPE)) {
            logger.info("started task: {}", job);
        } else {
            logger.debug("job started: {}", job);
        }
    }

    @Subscribe
    public void progressUpdate(JobEvent.ProgressUpdate jp) {
        TrackedJob job = jp.getJob();
        TrackedJob parent = job.getParent();

        if (job.getType().equals(ExperimentJob.JOB_TYPE)) {
            logger.info("eval job progress update: {}", jp);
        } else if (parent != null && parent.getType().equals(ExperimentJob.JOB_TYPE)) {
            logger.info("eval progress update: {}", jp);
        } else {
            logger.debug("progress: {}", jp);
        }
    }

    @Subscribe
    public void jobFinished(JobEvent.Finished jf) {
        TrackedJob job = jf.getJob();
        TrackedJob parent = job.getParent();

        if (job.getType().equals(ExperimentJob.JOB_TYPE)) {
            assert parent != null;
            logger.info("finished eval job {} of {}: {}",
                        parent.getChildrenFinished(),
                        parent.getChildCount(), job.getDescription());
        } else if (parent != null && parent.getType().equals(ExperimentJob.JOB_TYPE)) {
            logger.info("finished task: {}", job);
        } else {
            logger.debug("job finished: {}", job);
        }
    }
}

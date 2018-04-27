/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
    private int nrunning = 0;

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
            nrunning += 1;
            logger.info("started eval job {} of {}: {}",
                        nrunning, parent.getChildCount(), job.getDescription());
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
            logger.info("finished eval job {} of {}: {} ({})",
                        parent.getChildrenFinished(),
                        parent.getChildCount(), job.getDescription(), job.getTimer());
        } else if (parent != null && parent.getType().equals(ExperimentJob.JOB_TYPE)) {
            logger.info("finished task {} in {}", job, job.getTimer());
        } else {
            logger.debug("job finished: {}", job);
        }
    }

    @Subscribe
    public void jobFailed(JobEvent.Failed jf) {
        logger.error("job {} failed: {}", jf.getJob().getDescription(), jf.getException());
    }
}

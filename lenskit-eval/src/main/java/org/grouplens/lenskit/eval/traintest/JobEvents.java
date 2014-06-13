/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.traintest;

/**
 * Events that occur in the course of running jobs.  Processed by {@link com.google.common.eventbus.EventBus}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JobEvents {
    /**
     * Create a new job-started event with the current thread.
     * @param job The job that is started.
     * @return The new job started event.
     */
    public static JobStarted started(TrainTestJob job) {
        return new JobStarted(job, Thread.currentThread());
    }

    /**
     * Create a new job-finished event.
     * @param job The job that is finished.
     * @return The event.
     */
    public static JobFinished finished(TrainTestJob job) {
        return new JobFinished(job);
    }

    /**
     * Create a new job-failed event.
     * @param job The job that failed.
     * @param err The exception.
     * @return The event.
     */
    public static JobFailed failed(TrainTestJob job, Throwable err) {
        return new JobFailed(job, err);
    }

    public static class JobStarted {
        private final TrainTestJob job;
        private final Thread thread;

        public JobStarted(TrainTestJob j, Thread th) {
            job = j;
            thread = th;
        }

        public TrainTestJob getJob() {
            return job;
        }

        public Thread getThread() {
            return thread;
        }
    }

    public static class JobFinished {
        private final TrainTestJob job;

        public JobFinished(TrainTestJob j) {
            job = j;
        }

        public TrainTestJob getJob() {
            return job;
        }
    }

    public static class JobFailed {
        private final TrainTestJob job;
        private final Throwable error;

        public JobFailed(TrainTestJob j, Throwable why) {
            job = j;
            error = why;
        }

        public TrainTestJob getJob() {
            return job;
        }

        public Throwable getError() {
            return error;
        }
    }
}

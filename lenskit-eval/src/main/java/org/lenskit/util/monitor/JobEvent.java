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

/**
 * Events reporting the status of a {@link TrackedJob}.
 */
public class JobEvent {
    private JobEvent() {}

    /**
     * Event fired when a job is started.
     */
    public static class Started extends JobEvent {
        private final TrackedJob job;

        Started(TrackedJob job) {
            this.job = job;
        }

        /**
         * Get the job in this event.
         * @return The job.
         */
        public TrackedJob getJob() {
            return job;
        }

        @Override
        public String toString() {
            return "Started{" + job + '}';
        }
    }

    /**
     * Event fired when a job is finished.
     */
    public static class Finished extends JobEvent {
        private final TrackedJob job;

        Finished(TrackedJob job) {
            this.job = job;
        }

        /**
         * Get the job in this event.
         * @return The job.
         */
        public TrackedJob getJob() {
            return job;
        }

        @Override
        public String toString() {
            return "Finished{" + job + '}';
        }
    }

    /**
     * Event fired when a job has failed.
     */
    public static class Failed extends JobEvent {
        private final TrackedJob job;
        private final Throwable exception;

        Failed(TrackedJob job, Throwable th) {
            this.job = job;
            this.exception = th;
        }

        /**
         * Get the job in this event.
         * @return The job.
         */
        public TrackedJob getJob() {
            return job;
        }

        /**
         * Get the exception associated with this failure, if any.
         * @return The exception.
         */
        public Throwable getException() {
            return exception;
        }

        @Override
        public String toString() {
            return "Failed{" + job + ", " + exception +  '}';
        }
    }

    /**
     * Event fired when a job has a status update.
     */
    public static class ProgressUpdate extends JobEvent {
        private final TrackedJob job;
        private final int stepsDone;

        ProgressUpdate(TrackedJob job, int ndone) {
            this.job = job;
            stepsDone = ndone;
        }

        /**
         * Get the job in this event.
         * @return The job.
         */
        public TrackedJob getJob() {
            return job;
        }

        /**
         * Get the number of steps done.
         * @return The number of steps completed.
         */
        public int getStepsDone() {
            return stepsDone;
        }

        /**
         * Get the total steps.
         * @return The total number of steps (estimated).
         */
        public int getTotalSteps() {
            return job.getExpectedSteps();
        }

        @Override
        public String toString() {
            return "ProgressUpdate{" + job + ": " + stepsDone + "/" + getTotalSteps() + "}";
        }
    }
}

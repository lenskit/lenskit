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

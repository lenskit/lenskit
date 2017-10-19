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

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A job graph node used for tracking the status of work.
 */
public class TrackedJob {
    @Nullable
    private final TrackedJob parent;
    @Nonnull
    private final EventBus eventBus;
    @Nonnull
    private final String type;
    @Nullable
    private final String description;
    private final UUID uuid;
    private final Stopwatch timer;
    private @Nullable Throwable exception;

    private volatile int expectedSteps = -1;
    private volatile int stepsFinished = 0;
    private volatile int reportingInterval = 1;
    private volatile int childCount;
    private volatile int childrenRunning;
    private volatile int childrenFinished;

    /**
     * Create a new tracked job.
     * @param type The job type code.
     */
    public TrackedJob(String type) {
        this(type, null);
    }

    /**
     * Create a new tracked job.
     * @param type The job type code.
     * @param desc The description.
     */
    public TrackedJob(String type, String desc) {
        parent = null;
        eventBus = new EventBus();
        this.type = type;
        this.description = desc;
        uuid = UUID.randomUUID();
        timer = Stopwatch.createUnstarted();
    }

    private TrackedJob(TrackedJob parent, String type, String description) {
        this.parent = parent;
        this.type = type;
        this.description = description;
        this.eventBus = parent.getEventBus();
        this.uuid = UUID.randomUUID();
        timer = Stopwatch.createUnstarted();
    }

    @Nonnull
    public EventBus getEventBus() {
        return eventBus;
    }

    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the parent job.
     * @return The parent job.
     */
    @Nullable
    public TrackedJob getParent() {
        return parent;
    }

    /**
     * Create a child job.
     * @param type The job type.
     * @return The child job.
     */
    public TrackedJob makeChild(@Nonnull String type) {
        return makeChild(type, null);
    }

    /**
     * Create a child job.
     * @param type The job type.
     * @param description The job description.
     * @return The child job.
     */
    public TrackedJob makeChild(@Nonnull String type, @Nullable String description) {
        childCount += 1;
        return new TrackedJob(this, type, description);
    }

    @Nonnull
    public String getType() {
        return type;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    /**
     * Get the estimated number of steps.  This does *not* include children.
     * @return The estimated number of steps, or -1 if no estimate is available.
     */
    public int getExpectedSteps() {
        return expectedSteps;
    }

    /**
     * Get the number of finished steps.
     * @return The finished steps.
     */
    public int getStepsFinished() {
        return stepsFinished;
    }

    /**
     * Get the number of children.
     * @return The number of children.
     */
    public int getChildCount() {
        return childCount;
    }

    /**
     * Get the number of running children.
     * @return The number of running children.
     */
    public int getChildrenRunning() {
        return childrenRunning;
    }

    /**
     * Get the number of finished children.
     * @return The number of finished children.
     */
    public int getChildrenFinished() {
        return childrenFinished;
    }

    /**
     * Get the reporting interval for firing progress update messages.
     * @return The reporting interval.
     */
    public int getReportingInterval() {
        return reportingInterval;
    }

    /**
     * Set the reporting interval for firing progress update messages.
     * @param iv The reporting interval.
     */
    public void setReportingInterval(int iv) {
        reportingInterval = iv;
    }

    /**
     * Mark a step as finished.
     */
    public void finishStep() {
        finishSteps(1);
    }

    /**
     * Mark steps as finished.
     * @param n The number of additional steps that have been finished.
     */
    public void finishSteps(int n) {
        boolean fire = false;
        int riv = reportingInterval;
        synchronized (this) {
            int osteps = stepsFinished;
            stepsFinished += n;
            if (stepsFinished == expectedSteps || stepsFinished % riv > osteps % riv) {
                fire = true;
            }
        }
        if (fire) {
            eventBus.post(new JobEvent.ProgressUpdate(this, stepsFinished));
        }
    }

    /**
     * Start the job.
     */
    public void start() {
        if (parent != null) {
            synchronized (parent) {
                parent.childrenRunning += 1;
            }
        }
        timer.start();
        eventBus.post(new JobEvent.Started(this));
    }

    /**
     * Start the job with an estimated number of steps.
     * @param steps The estimated number of steps.
     */
    public void start(int steps) {
        expectedSteps = steps;
        start();
    }

    /**
     * Record the job as being finished.
     */
    public void finish() {
        timer.stop();
        if (parent != null) {
            synchronized (parent) {
                parent.childrenFinished += 1;
                parent.childrenRunning -= 1;
            }
        }
        eventBus.post(new JobEvent.Finished(this));
    }

    /**
     * Record the job as being failed.
     * @param th The error that is causing the job to fail.
     */
    public void fail(@Nullable Throwable th) {
        timer.stop();
        exception = th;
        eventBus.post(new JobEvent.Failed(this, exception));
    }

    /**
     * Get the exception with which this job failed.
     * @return The exception for this job, if there is one.
     */
    public @Nullable Throwable getException() {
        return exception;
    }

    /**
     * Get the timer for this job.
     */
    public Stopwatch getTimer() {
        return timer;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent.toString())
              .append(":");
        }
        sb.append(type);
        if (description != null) {
            sb.append("[")
              .append(description)
              .append("]");
        }
        return sb.toString();
    }
}

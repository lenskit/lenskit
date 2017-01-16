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
    }

    private TrackedJob(TrackedJob parent, String type, String description) {
        this.parent = parent;
        this.type = type;
        this.description = description;
        this.eventBus = parent.getEventBus();
        this.uuid = UUID.randomUUID();
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
        if (parent != null) {
            synchronized (parent) {
                parent.childrenFinished += 1;
                parent.childrenRunning -= 1;
            }
        }
        eventBus.post(new JobEvent.Finished(this));
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

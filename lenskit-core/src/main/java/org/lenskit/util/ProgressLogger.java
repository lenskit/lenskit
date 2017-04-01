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
package org.lenskit.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for logging progress of some long-running process.
 */
public class ProgressLogger {
    private final Logger logger;
    private final Stopwatch timer;
    private String label = "unspecified";
    private int total = -1;
    private int period = 100;
    private int prevN;
    private long prevMicros;
    private AtomicInteger ndone = new AtomicInteger(0);
    private BatchedMeanSmoother smoother = new BatchedMeanSmoother(0);

    ProgressLogger(Logger log) {
        logger = log;
        timer = Stopwatch.createUnstarted();
    }

    /**
     * Create a new progress logger.
     * @param log The logger to which progress should be logged.
     * @return The progress logger.
     */
    public static ProgressLogger create(Logger log) {
        return new ProgressLogger(log);
    }

    /**
     * Set the label to be used in log messages.
     * @param lbl The label.
     * @return The logger (for chaining).
     */
    public ProgressLogger setLabel(String lbl) {
        label = lbl;
        return this;
    }

    /**
     * Set the number of total items to be processed.
     * @param n The total number of items to be processed.
     * @return The logger (for chaining).
     */
    public ProgressLogger setCount(int n) {
        total = n;
        return this;
    }

    /**
     * Set the period for progress messages.
     * @param p The progress message period.
     * @return The logger (for chaining).
     */
    public ProgressLogger setPeriod(int p) {
        period = p;
        return this;
    }

    /**
     * Set the window to use for smoothing averages.
     * @param w The window size, in number of periods.
     * @return The progress logger (for chaining).
     */
    public ProgressLogger setWindow(int w) {
        smoother = new BatchedMeanSmoother(w);
        return this;
    }

    /**
     * Start the progress logger's timer.
     * @return The progress logger (for chaining).
     */
    public ProgressLogger start() {
        timer.start();
        return this;
    }

    /**
     * Record that an item has been completed.
     */
    public void advance() {
        Preconditions.checkState(timer.isRunning(), "progress logger not running");
        int n = ndone.incrementAndGet();
        if (logger.isTraceEnabled()) {
            logger.trace("finished {} of {} items", n, total);
        }
        if (n % period == 0) {
            logProgress();
        }
    }

    /**
     * Log the current progress to the logger.
     */
    public synchronized void logProgress() {
        long micros = timer.elapsed(TimeUnit.MICROSECONDS);
        long elapsed = micros - prevMicros;
        prevMicros = micros;
        double time = elapsed * 0.000001;
        int ndone = this.ndone.get();
        int n = ndone - prevN;
        prevN = ndone;
        smoother.addBatch(n, time);
        double rate = smoother.currentAverage();
        if (total >= 0) {
            double est = (total - ndone) * rate;
            logger.info("{}: finished {} of {} ({}%, {}s/row, ETA {})",
                        label, ndone, total,
                        String.format("%.2f", (((double) ndone) * 100) / total),
                        String.format("%.3f", rate),
                        formatElapsedTime(est));
        } else {
            logger.info("{}: finished {} ({}s/row)",
                        label, ndone, String.format("%.3f", rate));
        }
    }

    /**
     * Log that the process has finished.
     * @return The number of seconds.
     */
    public double finish() {
        timer.stop();
        int ndone = this.ndone.get();
        if (ndone < total) {
            logger.warn("{}: only performed {} of {} actions", label, ndone, total);
        }
        double time = timer.elapsed(TimeUnit.MILLISECONDS) * 0.001;
        logger.debug("{}: finished {} in {} ({}s/item)",
                     label, ndone,
                     formatElapsedTime(time),
                     String.format("%.3f", time / ndone));
        return time;
    }

    /**
     * Get a string representation of the elapsed time.
     * @return The elapsed string as a human-friendly string.
     */
    public String elapsedTime() {
        return formatElapsedTime(timer.elapsed(TimeUnit.MILLISECONDS) * 0.001);
    }

    /**
     * Format elapsed time.
     * @param seconds The number of seconds that have elapsed.
     * @return The time, formatted as a string.
     */
    public static String formatElapsedTime(double seconds) {
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        double secs = seconds % 60;
        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append('h');
        }
        if (hours > 0 || minutes > 0) {
            sb.append(String.format("%2dm", minutes));
        }
        sb.append(String.format("%.3fs", secs));
        return sb.toString();
    }
}

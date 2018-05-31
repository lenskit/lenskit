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
    private long timePeriod = TimeUnit.MICROSECONDS.convert(30, TimeUnit.SECONDS);
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
     * Set a time period for reporting progress.
     * @param secs The time period for reporting progress.
     * @return The logger (for chaining).
     */
    public ProgressLogger setTimePeriod(long secs) {
        timePeriod = TimeUnit.MICROSECONDS.convert(secs, TimeUnit.SECONDS);
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
        } else {
            // log every 30 seconds
            long micros = timer.elapsed(TimeUnit.MICROSECONDS);
            if (micros - prevMicros > timePeriod) {
                logProgress(micros);
            }
        }
    }

    /**
     * Log the current progress to the logger.
     */
    public synchronized void logProgress() {
        long micros = timer.elapsed(TimeUnit.MICROSECONDS);
        logProgress(micros);
    }

    private void logProgress(long micros) {
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

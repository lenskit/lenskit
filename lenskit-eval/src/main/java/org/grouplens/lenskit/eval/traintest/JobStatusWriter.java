/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.google.common.collect.*;
import com.google.common.eventbus.Subscribe;
import com.google.common.io.Files;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Component that writes the job status to a file.
 *
 * <p>The methods on this class are synchronized because, while the same method will not be called
 * multiple times concurrently by the event bus, it does not say that different methdos on the same
 * object will not be.</p>
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class JobStatusWriter {
    private static final Logger logger = LoggerFactory.getLogger(JobStatusWriter.class);
    private final TrainTestEvalTask task;
    private final File outputFile;
    private Set<TrainTestJob> finishedJobs, activeJobs, allJobs;
    private BiMap<TrainTestJob,Thread> threads;
    private Map<TrainTestJob,DateTime> startTime;

    /**
     * Create a new job status writer.
     * @param file The output file.
     */
    public JobStatusWriter(TrainTestEvalTask task, Set<TrainTestJob> jobs, File file) {
        this.task = task;
        outputFile = file;
        finishedJobs = Sets.newHashSet();
        activeJobs = Sets.newHashSet();
        allJobs = ImmutableSet.copyOf(jobs);
        threads = HashBiMap.create();
        startTime = Maps.newHashMap();
    }

    @Subscribe
    public synchronized void jobStarted(JobEvents.JobStarted event) {
        TrainTestJob job = event.getJob();
        if (!job.getTask().equals(task)) {
            return;
        }

        logger.debug("received event: started {}", job);
        activeJobs.add(job);
        threads.forcePut(job, event.getThread());
        startTime.put(job, DateTime.now());
        writeStatus();
    }

    @Subscribe
    public synchronized void jobFinished(JobEvents.JobFinished event) {
        TrainTestJob job = event.getJob();
        if (!job.getTask().equals(task)) {
            return;
        }

        logger.debug("received event: finished {}", job);
        activeJobs.remove(job);
        finishedJobs.add(job);
        threads.remove(job);
        writeStatus();
    }

    @Subscribe
    public synchronized void jobFailed(JobEvents.JobFailed event) {
        TrainTestJob job = event.getJob();
        if (!job.getTask().equals(task)) {
            return;
        }

        logger.debug("received event: failed {}", job);
        activeJobs.remove(job);
        finishedJobs.add(job);
        threads.remove(job);
        writeStatus();
    }

    private void writeStatus() {
        try {
            DateTimeFormatter format = DateTimeFormat.longDateTime();
            DateTime now = DateTime.now();
            Files.createParentDirs(outputFile);
            PrintWriter writer = new PrintWriter(outputFile);
            try {
                writer.println("status_time: " + format.print(now));
                writer.println("summary:");
                writer.println("  total: " + allJobs.size());
                writer.println("  active: " + activeJobs.size());
                writer.println("  completed: " + finishedJobs.size());
                writer.println("running_tasks:");
                for (TrainTestJob job: activeJobs) {
                    writer.print("- task: ");
                    writer.println(job);
                    DateTime start = startTime.get(job);
                    writer.format("  started: %s\n", format.print(start));
                    writer.println("  thread: " + threads.get(job).getName());
                }
            } finally {
                writer.close();
            }
        } catch (Exception ex) {
            logger.error("failed to write status", ex);
        }
    }
}

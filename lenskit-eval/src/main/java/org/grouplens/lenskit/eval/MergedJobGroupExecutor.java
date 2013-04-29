/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.grouplens.lenskit.util.parallel.ExecHelpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Run the jobs from a set of job groups in a single work queue, allowing the
 * groups to overlap.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class MergedJobGroupExecutor implements JobGroupExecutor {
    private static final Logger logger = LoggerFactory.getLogger(MergedJobGroupExecutor.class);

    enum JobGroupState {
        WAITING, RUNNING, FINISHED
    }

    private int threadCount;
    private List<JobGroup> groups;
    private Reference2IntMap<Job> jobGroupMap;
    private int[] pendingJobCounts;
    private JobGroupState[] groupStates;

    public MergedJobGroupExecutor(int threads) {
        threadCount = threads;
        groups = new ArrayList<JobGroup>();
        jobGroupMap = new Reference2IntOpenHashMap<Job>();
        jobGroupMap.defaultReturnValue(-1);
    }


    @Override
    public void add(JobGroup group) {
        final int num = groups.size();
        groups.add(group);
        assert groups.get(num) == group;

        for (Job job : group.getJobs()) {
            if (jobGroupMap.containsKey(job)) {
                throw new IllegalStateException("Job " + job.getName()
                                                        + " appears more than once");
            }
            jobGroupMap.put(job, num);
        }
    }

    @Override
    public void run() throws ExecutionException {
        if (groupStates != null || pendingJobCounts != null) {
            throw new IllegalStateException("Executor already running");
        }

        ExecutorService svc = Executors.newFixedThreadPool(threadCount);

        final int ngroups = groups.size();
        groupStates = new JobGroupState[ngroups];
        pendingJobCounts = new int[ngroups];

        List<Runnable> tasks = new ArrayList<Runnable>();
        for (int i = 0; i < ngroups; i++) {
            groupStates[i] = JobGroupState.WAITING;
            JobGroup group = groups.get(i);
            pendingJobCounts[i] = group.getJobs().size();
            for (Job job : group.getJobs()) {
                tasks.add(new JobTask(job));
            }
        }

        try {
            ExecHelpers.parallelRun(svc, tasks);
        } catch (RuntimeException err) {
            throw err;
        } catch (ExecutionException err) {
            throw err;
        } finally {
            pendingJobCounts = null;
            groupStates = null;
            svc.shutdownNow();
        }
    }

    /**
     * Start a job, calling its group's {@link JobGroup#start()} method if
     * necessary.
     *
     * @param job
     */
    private synchronized void jobStarting(Job job) {
        logger.debug("Starting job {}", job.getName());
        int gnum = jobGroupMap.getInt(job);
        assert groupStates[gnum] != JobGroupState.FINISHED;
        if (groupStates[gnum] == JobGroupState.WAITING) {
            JobGroup group = groups.get(gnum);
            logger.info("Starting job group {}", group.getName());
            group.start();
            groupStates[gnum] = JobGroupState.RUNNING;
        }
    }

    /**
     * Finalize a job, calling the group's {@link JobGroup#finish()} method if
     * appropriate.
     *
     * @param job
     */
    private synchronized void jobFinished(Job job) {
        logger.debug("Finished job {}", job.getName());
        int gnum = jobGroupMap.getInt(job);
        assert pendingJobCounts[gnum] > 0;
        assert groupStates[gnum] == JobGroupState.RUNNING;
        // the task has already invoked the listener jobFinished
        if (--pendingJobCounts[gnum] == 0) {
            JobGroup group = groups.get(gnum);
            logger.info("Finishing job group {}", group.getName());
            group.finish();
            groupStates[gnum] = JobGroupState.FINISHED;
        }
    }

    /**
     * Run a job, calling {@link MergedJobGroupExecutor#jobStarting(Job)} and
     * {@link MergedJobGroupExecutor#jobFinished(Job)} before and after.
     *
     * @author <a href="http://www.grouplens.org">GroupLens Research</a>
     */
    private class JobTask implements Runnable {
        final Job job;

        public JobTask(Job job) {
            this.job = job;
        }

        @Override
        public void run() {
            jobStarting(job);
            try {
                job.run();
            } catch (Throwable e) {
                if (e instanceof ThreadDeath) {
                    throw (ThreadDeath) e;
                }
                logger.error("Error running {}", job.getDescription(), e);
                throw new RuntimeException("Error running " + job.getName(), e);
            } finally {
                jobFinished(job);
            }
        }
    }
}

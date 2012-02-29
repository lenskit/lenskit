/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

/**
 * Configure and run evaluations.
 * 
 * <p>
 * Note: this class will be changing substantially in upcoming versions. The
 * current iteration is suitable for command line or Maven-based evaluations,
 * but will need adaptation to provide the eventual monitoring UI. The
 * {@link Runnable}-based interface may or may not be retained.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class EvalRunner {
    private static final Logger logger = LoggerFactory.getLogger(EvalRunner.class);

    private int threadCount = 1;
    private IsolationLevel isolationLevel = IsolationLevel.NONE;
    
    private Evaluation evaluation;
    private EvalListenerManager listeners = new EvalListenerManager();
    
    /**
     * Construct an evaluation runner from an evaluator name and configuration.
     * 
     * @param eval The evaluation to run.
     */
    public EvalRunner(Evaluation eval) {
        evaluation = eval;
    }
    
    /**
     * Get the eval runner thread count.    
     * @return The number of concurrent evaluations to allow.
     * @see #setThreadCount(int)
     */
    public int getThreadCount() {
        return threadCount;
    }
    
    /**
     * Set eval runner thread count.
     * 
     * @param threads The number of concurrent evaluations to allow. This has no
     *        impact on the number of threads used for building recommenders, if
     *        multithreaded recommenders are used.  If 0, then all available
     *        processors are used (from {@link Runtime#availableProcessors()}).
     *        The default is 1.
     * @return The evaluation runner for chaining.
     */
    public EvalRunner setThreadCount(int threads) {
        threadCount = threads;
        return this;
    }
    
    /**
     * Get the evaluation isolation level.
     * @see #setIsolationLevel(IsolationLevel)
     */
    public IsolationLevel getIsolationLevel() {
        return isolationLevel;
    }
    
    /**
     * Set the isolation level for this evaluation.
     * 
     * @param level The isolation level to use. The default is
     *        {@link IsolationLevel#NONE}.
     * @return The evaluation runner for chaining.
     */
    public EvalRunner setIsolationLevel(IsolationLevel level) {
        isolationLevel = level;
        return this;
    }
    
    public void addListener(EvaluationListener listener) {
        listeners.addListener(listener);
    }

    public void removeListener(EvaluationListener listener) {
        listeners.removeListener(listener);
    }

    /**
     * Prepare all job groups.
     * 
     * @param context The preparation context to use.
     */
    public void prepare(PreparationContext context) throws PreparationException {
        for (JobGroup group: evaluation.getJobGroups()) {
            logger.debug("Requesting to prepare group {}", group.getName());
            context.prepare(group);
        }
    }
    
    /**
     * Run the evaluation.  This method assumes that the evaluation is already
     * prepared — call {@link #prepare(PreparationContext)} to do that.
     */
    public void run() throws ExecutionException {
        int nthreads = threadCount;
        if (nthreads <= 0) {
            nthreads = Runtime.getRuntime().availableProcessors();
        }
        
        logger.info("Starting evaluation");
        evaluation.start();
        
        logger.info("Running evaluator with {} threads", nthreads);
        JobGroupExecutor exec;
        switch (isolationLevel) {
        case NONE:
            exec = new MergedJobGroupExecutor(nthreads, listeners);
            break;
        case JOB_GROUP:
            exec = new SequentialJobGroupExecutor(nthreads, listeners);
            break;
        default:
            throw new RuntimeException("Invalid isolation level " + isolationLevel);
        }
        
        for (JobGroup group: evaluation.getJobGroups()) {
            exec.add(group);
        }
        try {
            exec.run();
        } finally {
            logger.info("Finishing evaluation");
            evaluation.finish();
        }
    }
}

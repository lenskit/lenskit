/*
 * LensKit, a reference implementation of recommender algorithms.
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

/**
 * Listener to receive updates on evaluation progress. Unless otherwise
 * specified, no guarantees are made as to what thread is used to invoke
 * listener methods; listeners need to handle inter-thread dispatch themselves.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @since 0.9
 */
public interface EvaluationListener {
    /**
     * Notify that the evaluation is about to begin.
     */
    void evaluationStarting();

    /**
     * Notify that the evaluation has finished.
     * 
     * @param err <tt>null</tt> if the evaluation completed successfully, or the
     *        exception thrown if it failed.
     */
    void evaluationFinished(Exception err);
    
    /**
     * Notify that a job group is starting.
     */
    void jobGroupStarting(JobGroup group);
    
    /**
     * Notify that a job group has finished executing.
     */
    void jobGroupFinished(JobGroup group);

    /**
     * Notify that a job is starting. The executor will invoke this method on
     * the thread which is running the job.
     */
    void jobStarting(Job job);

    /**
     * Notify that a job is finished. The executor will invoke this method on
     * the thread which ran the job.
     * 
     * @param job The job that finished.
     * @param err <tt>null</tt> if the job finished successfully, or the
     *        exception thrown if it failed.
     */
    void jobFinished(Job job, Exception err);
}

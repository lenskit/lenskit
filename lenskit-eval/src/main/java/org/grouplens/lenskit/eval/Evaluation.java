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

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A set of job groups comprising an evaluation. Evaluations are configured and
 * set up by {@link Evaluator}s. They contain {@link JobGroup}s that the runner
 * should run in order to complete the evaluation.
 * 
 * @since 0.8
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface Evaluation {
    /**
     * Start the evaluation.  The evaluation runner calls this method before
     * starting to run any jobs.  It is responsible for getting ready for
     * job groups to run.
     */
    void start();
    
    /**
     * Finalize the evaluation.  This method is called after all jobs have
     * finished.  It is responsible for closing any output files, freeing
     * remaining resources, etc.
     */
    void finish();
    
    /**
     * Get the job groups comprising this evaluation.
     * 
     * @return A list of the job groups to run for this evaluation.
     */
    @Nonnull List<JobGroup> getJobGroups();
}

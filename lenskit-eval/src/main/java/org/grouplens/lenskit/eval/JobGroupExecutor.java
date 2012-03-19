/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.util.concurrent.ExecutionException;

/**
 * Execute job groups. {@link JobGroup}s are added to the executor, and then
 * they are all run when {@link #run()} is called.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface JobGroupExecutor {
    /**
     * Add a job group to be executed by this executor.
     * @param group The job group to execute.
     */
    void add(JobGroup group);

    int getThreadCount();
    
    /**
     * Run the job groups.
     * @throws ExecutionException if one of the jobs fails.
     */
    void run() throws ExecutionException;
}

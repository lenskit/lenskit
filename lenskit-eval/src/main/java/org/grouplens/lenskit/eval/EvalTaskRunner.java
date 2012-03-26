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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Run eval tasks, making sure their dependencies have been executed first.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class EvalTaskRunner {

    private static final Logger logger = LoggerFactory.getLogger(EvalTaskRunner.class);

    private int threadCount = 1;
    private GlobalEvalOptions options;
    private Set<EvalTask> completed;

    
    public EvalTaskRunner(GlobalEvalOptions opt) {
        options = opt;
        completed = new HashSet<EvalTask>();
    }

    /**
     * The executor first checks if the dependency has been executed and resolve all the dependencies recursively
     * before execute the input task
     *
     * @param task The task to be executed.
     * @throws EvalTaskFailedException if the task failed to execute.
     */
    public void execute(EvalTask task) throws EvalTaskFailedException {
        if(!completed.contains(task)) {
            Set<EvalTask> depends = task.getDependencies();
            for(EvalTask t: depends) {
                execute(t);
            }
            // run the task, turning *all* failures into checked exceptions
            try {
                task.execute(options);
            } catch (RuntimeException e) {
                throw new EvalTaskFailedException("runtime exception running task", e);
            }
            completed.add(task);
        }
    }

}

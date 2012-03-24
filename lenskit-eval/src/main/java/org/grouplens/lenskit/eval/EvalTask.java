package org.grouplens.lenskit.eval;

import java.util.Set;

/**
 *  An evaluation task has dependencies to resolve before running.
 *
 *  @author Shuo Chang<schang@cs.umn.edu>
 */
public interface EvalTask  {

    /**
     * Get the name of the task
     *
     * @return name of the task
     */
    String getName();

    /**
     *  Get the set of dependent tasks
     *
      * @return the set of dependencies
     */
    Set<EvalTask> getDependencies();

    /**
     * Run the evaluation task
     *
     * @param options options that may affect the behavior of the task
     * @throws EvalTaskFailedException
     */
    void execute(GlobalEvalOptions options) throws EvalTaskFailedException;

}

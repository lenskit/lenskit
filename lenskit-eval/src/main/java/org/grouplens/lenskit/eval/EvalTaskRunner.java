package org.grouplens.lenskit.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The runner resolves the dependencies of the evaluation task recursively and execute the evaluation task.
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
     * @throws EvalTaskFailedException
     */
    public void execute(EvalTask task) throws EvalTaskFailedException {
        if(!completed.contains(task)) {
            Set<EvalTask> depends = task.getDependencies();
            for(EvalTask t: depends) {
                execute(t);
            }
            task.execute(options);
            completed.add(task);
        }
    }

}

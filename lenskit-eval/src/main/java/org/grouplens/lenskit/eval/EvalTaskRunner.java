package org.grouplens.lenskit.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/20/12
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
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

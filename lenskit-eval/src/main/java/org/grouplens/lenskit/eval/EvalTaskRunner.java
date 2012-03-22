package org.grouplens.lenskit.eval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
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
    private EvalTaskOptions options;
    private Set<EvalTask> completed;
    private Set<EvalTask> pending;
    
    public EvalTaskRunner(EvalTaskOptions opt) {
        options = opt;
        completed = new HashSet<EvalTask>();
        pending = new HashSet<EvalTask>();
    }

    public void addTask(EvalTask task) {
        pending.add(task);
    }

    public void run() throws EvalExecuteException {
        while (!pending.isEmpty()) {
            for(EvalTask t : pending) {
                if(checkDependencies(t)) {
                    t.call(options);
                }
            }

        }
    }

    protected boolean checkDependencies(EvalTask task) {
        Set<EvalTask> depends = task.getDependencies();
        boolean result = true;
        if(!depends.isEmpty()) {
            for(EvalTask t : depends) {
                result = result & completed.contains(t);
            }
        }
        return result;
    }
    
    

}

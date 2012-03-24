package org.grouplens.lenskit.eval;

import java.util.Set;

/**
 *  The abstract class of EvalTask
 *
 *  @author Shuo Chang<schang@cs.umn.edu>
 */
public abstract class AbstractEvalTask implements EvalTask{
    protected final String name;
    protected Set<EvalTask> dependencies;

    protected AbstractEvalTask(String name, Set<EvalTask> dependencies) {
        this.name = name;
        this.dependencies = dependencies;
    }

    public String getName() {
        return name;
    }
    
    public Set<EvalTask> getDependencies() {
        return dependencies;
    }

    public abstract void execute(GlobalEvalOptions options) throws EvalTaskFailedException;
    
}

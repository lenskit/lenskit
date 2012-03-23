package org.grouplens.lenskit.eval;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/14/12
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
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

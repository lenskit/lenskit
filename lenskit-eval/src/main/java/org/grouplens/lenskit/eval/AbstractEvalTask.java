package org.grouplens.lenskit.eval;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/14/12
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractEvalTask implements EvalTask<Void>{
    protected final String name;
    protected Set<EvalTask> dependency;

    protected AbstractEvalTask(String name, Set<EvalTask> dependency) {
        this.name = name;
        this.dependency = dependency;
    }

    public String getName() {
        return name;
    }
    
    public Set<EvalTask> getDependency() {
        return dependency;
    }

    public abstract Void call() throws Exception;
    
}

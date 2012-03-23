package org.grouplens.lenskit.eval;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.config.BuilderFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: schang
 * Date: 3/14/12
 * Time: 11:57 AM
 * To change this template use File | Settings | File Templates.
 *
 */
public abstract class AbstractEvalTaskBuilder {
    protected String name;
    protected Set<EvalTask> dependencies = new HashSet<EvalTask>();

    protected AbstractEvalTaskBuilder() {}

    protected AbstractEvalTaskBuilder(String name) {
        this.name = name;
    }

    /**
     * Get the algorithm name.
     * @return The name for this algorithm instance.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the algorithm name.
     * @param n The name for this algorithm instance.
     * @return The builder for chaining.
     */
    public AbstractEvalTaskBuilder setName(String n) {
        name = n;
        return this;
    }
    
    public Set<EvalTask> getDependencies() {
        return dependencies;
    }

    public AbstractEvalTaskBuilder addDependency(EvalTask task) {
        dependencies.add(task);
        return this;
    }

    public AbstractEvalTaskBuilder addDepends(EvalTask task) {
        dependencies.add(task);
        return this;
    }
}

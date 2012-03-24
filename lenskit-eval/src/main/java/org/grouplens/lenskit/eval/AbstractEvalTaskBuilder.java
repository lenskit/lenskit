package org.grouplens.lenskit.eval;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.eval.config.BuilderFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * The abstract builder for the AbstractEvalTask
 *
 * @author Shuo Chang<schang@cs.umn.edu>
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

    /**
     * Add a EvalTask to the dependencies set
     * @param task The dependency task to add
     * @return The builder for chaining
     */
    public AbstractEvalTaskBuilder addDependency(EvalTask task) {
        dependencies.add(task);
        return this;
    }

    /**
     * The same with {@link #addDependency(EvalTask)}
     *
     */
    public AbstractEvalTaskBuilder addDepends(EvalTask task) {
        dependencies.add(task);
        return this;
    }
}

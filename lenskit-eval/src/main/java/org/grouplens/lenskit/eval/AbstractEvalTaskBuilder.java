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
public abstract class AbstractEvalTaskBuilder<T extends EvalTask> implements Builder<T> {
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

    // need redeclaration due to Java type system issues
    public abstract T build();
}

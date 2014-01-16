/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.inject;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.CachePolicy;
import org.grouplens.grapht.reflect.CachedSatisfaction;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.solver.*;
import org.grouplens.lenskit.core.LenskitConfiguration;

import java.util.List;
import java.util.Set;

/**
 * Build recommender graphs.  You probably don't want to use this class.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class RecommenderGraphBuilder {
    private static final int RESOLVE_DEPTH_LIMIT = 100;

    private ClassLoader classLoader;
    private List<BindingFunctionBuilder> configs = Lists.newArrayList();
    private Set<Class<?>> roots = Sets.newHashSet();
    private Function<BindingFunction, BindingFunction> bindingTransform = Functions.identity();

    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * Set a function to transform the bind rules used to build the dependency solver.
     * @param func The transform function.
     * @return The builder (for chaining).
     */
    public RecommenderGraphBuilder setBindingTransform(Function<BindingFunction,BindingFunction> func) {
        bindingTransform = func;
        return this;
    }

    /**
     * Add bindings to the graph builder.  Bindings added to this builder are processed in the
     * <em>reverse</em> order as {@link DependencySolverBuilder} - add the most important bindings
     * last.
     *
     * @param bld A builder of the bindings to add.
     * @return The graph builder.
     */
    public RecommenderGraphBuilder addBindings(BindingFunctionBuilder bld) {
        configs.add(bld);
        return this;
    }

    /**
     * Add roots to the graph builder.
     * @param classes Root types for the graph.
     * @return The graph builder (for chaining).
     */
    public RecommenderGraphBuilder addRoots(Iterable<Class<?>> classes) {
        Iterables.addAll(roots, classes);
        return this;
    }

    /**
     * Add a recommender configuration.
     * @param config The configuration.
     * @return The graph builder (for chaining).
     */
    public RecommenderGraphBuilder addConfiguration(LenskitConfiguration config) {
        addBindings(config.getBindings());
        addRoots(config.getRoots());
        return this;
    }

    /**
     * Build a dependency solver from the provided bindings.
     *
     * @return The dependency solver.
     */
    public DependencySolver buildDependencySolver() {
        DependencySolverBuilder dsb = DependencySolver.newBuilder();
        for (BindingFunctionBuilder cfg: Lists.reverse(configs)) {
            dsb.addBindingFunction(bindingTransform.apply(cfg.build(BindingFunctionBuilder.RuleSet.EXPLICIT)));
            dsb.addBindingFunction(bindingTransform.apply(cfg.build(BindingFunctionBuilder.RuleSet.INTERMEDIATE_TYPES)));
            dsb.addBindingFunction(bindingTransform.apply(cfg.build(BindingFunctionBuilder.RuleSet.SUPER_TYPES)));
        }
        // default desire function cannot trigger rewrites
        dsb.addBindingFunction(DefaultDesireBindingFunction.create(classLoader), false);
        dsb.setDefaultPolicy(CachePolicy.MEMOIZE);
        dsb.setMaxDepth(RESOLVE_DEPTH_LIMIT);
        return dsb.build();
    }

    public DAGNode<CachedSatisfaction,DesireChain> buildGraph() throws SolverException {
        DependencySolver solver = buildDependencySolver();
        for (Class<?> root: roots) {
            solver.resolve(Desires.create(null, root, true));
        }

        return solver.getGraph();
    }
}

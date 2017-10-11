/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.inject;

import com.google.common.collect.*;
import org.grouplens.grapht.*;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.solver.*;
import org.lenskit.LenskitConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Build recommender graphs.  You probably don't want to use this class.
 *
 * @compat private
 * @since 2.1
 */
public class RecommenderGraphBuilder {
    private static final int RESOLVE_DEPTH_LIMIT = 100;

    private ClassLoader classLoader;
    private List<BindingFunctionBuilder> configs = Lists.newArrayList();
    private Set<Class<?>> roots = Sets.newHashSet();

    public void setClassLoader(ClassLoader loader) {
        classLoader = loader;
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
        return buildDependencySolverImpl(SolveDirection.SOLVE);
    }

    /**
     * Build a dependency 'unsolver' from the provided bindings. The resulting solver, when rewriting
     * a graph, will replace bound targets with placeholders.
     * @return The dependency solver.
     */
    public DependencySolver buildDependencyUnsolver() {
        return buildDependencySolverImpl(SolveDirection.UNSOLVE);
    }

    public DependencySolver buildDependencySolverImpl(SolveDirection direction) {
        DependencySolverBuilder dsb = DependencySolver.newBuilder();
        for (BindingFunctionBuilder cfg: Lists.reverse(configs)) {
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.EXPLICIT)));
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.INTERMEDIATE_TYPES)));
            dsb.addBindingFunction(direction.transform(cfg.build(BindingFunctionBuilder.RuleSet.SUPER_TYPES)));
        }
        // default desire function cannot trigger rewrites
        dsb.addBindingFunction(DefaultDesireBindingFunction.create(classLoader), false);
        dsb.setDefaultPolicy(CachePolicy.MEMOIZE);
        dsb.setMaxDepth(RESOLVE_DEPTH_LIMIT);
        return dsb.build();
    }

    public DAGNode<Component,Dependency> buildGraph() throws ResolutionException {
        DependencySolver solver = buildDependencySolver();
        for (Class<?> root: roots) {
            solver.resolve(Desires.create(null, root, true));
        }

        return solver.getGraph();
    }

    private static enum SolveDirection {
        SOLVE {
            @Override
            public BindingFunction transform(BindingFunction bindFunction) {
                return bindFunction;
            }
        },
        UNSOLVE {
            @Override
            public BindingFunction transform(BindingFunction bindFunction) {
                if (bindFunction instanceof RuleBasedBindingFunction) {
                    RuleBasedBindingFunction rbf = (RuleBasedBindingFunction) bindFunction;
                    ListMultimap<ContextMatcher, BindRule> bindings = rbf.getRules();
                    ListMultimap<ContextMatcher, BindRule> newBindings = ArrayListMultimap.create();
                    for (Map.Entry<ContextMatcher, BindRule> entry: bindings.entries()) {
                        BindRule rule = entry.getValue();
                        BindRuleBuilder builder = rule.newCopyBuilder();
                        Class<?> type = builder.getDependencyType();
                        newBindings.put(entry.getKey(),
                                        builder.setSatisfaction(new PlaceholderSatisfaction(type))
                                               .build());
                    }
                    return new RuleBasedBindingFunction(newBindings);
                } else {
                    throw new IllegalArgumentException("cannot transform bind function " + bindFunction);
                }
            }
        };

        public abstract BindingFunction transform(BindingFunction bindFunction);
    }
}

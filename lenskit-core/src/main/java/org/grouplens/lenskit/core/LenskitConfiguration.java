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
package org.grouplens.lenskit.core;

import com.google.common.collect.ImmutableSet;
import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.lenskit.*;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.grouplens.lenskit.core.ContextWrapper.coerce;

/**
 * A LensKit algorithm configuration.  Once you have a configuration, you can pass it to
 * {@link LenskitRecommenderEngine#build(LenskitConfiguration)}
 * to build a recommender engine, or {@link LenskitRecommender#build(LenskitConfiguration)}
 * to skip the engine and just build a recommender.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class LenskitConfiguration extends AbstractConfigContext {
    private static final Class<?>[] INITIAL_ROOTS = {
            RatingPredictor.class,
            ItemScorer.class,
            GlobalItemScorer.class,
            ItemRecommender.class,
            GlobalItemRecommender.class
    };
    public static final InjectSPI LENSKIT_SPI = new ReflectionInjectSPI();

    private final BindingFunctionBuilder bindings;
    private final Set<Class<?>> roots;

    public LenskitConfiguration() {
        bindings = new BindingFunctionBuilder(LENSKIT_SPI, true);
        roots = new HashSet<Class<?>>();
        Collections.addAll(roots, INITIAL_ROOTS);
    }

    /**
     * Create a new copy of a LensKit configuration.
     * @param other The configuration to copy.
     */
    public LenskitConfiguration(LenskitConfiguration other) {
        bindings = other.bindings.clone();
        roots = new HashSet<Class<?>>(other.roots);
    }

    /**
     * Convenience method to copy a LensKit configuration.
     * @return An independent copy of this configuration.
     */
    public LenskitConfiguration copy() {
        return new LenskitConfiguration(this);
    }

    InjectSPI getSPI() {
        return bindings.getSPI();
    }

    /**
     * Add the specified component type as a root component. This forces it (and its
     * dependencies) to be resolved, and makes it available from the resulting
     * recommenders.
     *
     * @param componentType The type of component to add as a root (typically an interface).
     * @see LenskitRecommender#get(Class)
     */
    public void addRoot(Class<?> componentType) {
        roots.add(componentType);
    }

    @Override
    public <T> LenskitBinding<T> bind(Class<T> type) {
        return ContextWrapper.coerce(bindings.getRootContext()).bind(type);
    }

    @Override
    public LenskitConfigContext within(Class<?> type) {
        return coerce(bindings.getRootContext().within(type));
    }

    @Override
    public LenskitConfigContext within(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext within(Annotation qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().within(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Class<?> type) {
        return coerce(bindings.getRootContext().at(type));
    }

    @Override
    public LenskitConfigContext at(Class<? extends Annotation> qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().at(qualifier, type));
    }

    @Override
    public LenskitConfigContext at(Annotation qualifier, Class<?> type) {
        return coerce(bindings.getRootContext().at(qualifier, type));
    }

    private void resolve(Class<?> type, DependencySolver solver) throws SolverException {
        solver.resolve(bindings.getSPI().desire(null, type, true));
    }

    BindingFunctionBuilder getBindings() {
        return bindings;
    }

    Set<Class<?>> getRoots() {
        return ImmutableSet.copyOf(roots);
    }

    /**
     * Get a mockup of the full recommender graph. This fully resolves the graph so that
     * it can be analyzed, but does not create any objects.
     *
     * @return The full graph.
     * @deprecated This shouldn't be used anymore.
     */
    @Deprecated
    public DAGNode<CachedSatisfaction, DesireChain> buildGraph() throws RecommenderConfigurationException {
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addBindings(bindings);
        rgb.addRoots(roots);
        try {
            return rgb.buildGraph();
        } catch (SolverException e) {
            throw new RecommenderConfigurationException("Cannot resolve configuration graph", e);
        }
    }
}

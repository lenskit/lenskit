/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.eval.algorithm;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.script.BuiltBy;
import org.grouplens.lenskit.inject.RecommenderGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

/**
 * An instance of a recommender algorithmInfo to be benchmarked.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@BuiltBy(AlgorithmInstanceBuilder.class)
public class AlgorithmInstance implements Attributed {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    @Nullable
    private final String algoName;
    @Nonnull
    private final LenskitConfiguration config;
    @Nonnull
    private final Map<String, Object> attributes;
    private final boolean preload;
    private Random random;

    /*
     * Takes the name and Lenskit configuration to generate an Algorithm instance
     */
    public AlgorithmInstance(String name, LenskitConfiguration config) {
        this(name, config, Collections.<String, Object>emptyMap(), false);
    }

    public AlgorithmInstance(String name, LenskitConfiguration cfg, Map<String, Object> attrs, boolean preload) {
        algoName = name;
        config = new LenskitConfiguration(cfg);
        attributes = ImmutableMap.copyOf(attrs);
        this.preload = preload;
    }


    /**
     * Get the name of this algorithmInfo.  This returns a short name which is
     * used to identify the algorithmInfo or instance.
     *
     * @return The algorithmInfo's name
     */
    @Override
    public String getName() {
        return algoName;
    }

    /**
     * Query whether this algorithmInfo is to operate on in-memory data.
     *
     * @return {@code true} if the ratings database should be loaded in-memory
     *         prior to running.
     */
    public boolean getPreload() {
        return preload;
    }

    @Override
    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Nonnull
    public LenskitConfiguration getConfig() {
        return config;
    }

    /**
     * Let AlgorithmInstanceBuilder to pass random number generator to algorithmInfo instance
     * 
     * @param rng The random number generator.
     * @return The new algorithmInfo instance
     */
    public AlgorithmInstance setRandom(Random rng) {
        random = rng;
        return this;
    }

    /**
     * Build a recommender.
     * @param defaults Additional configuration.  This configuration comes <em>before</em> the
     *                 algorithm's configuration, so it is overridden if appropriate.
     * @return
     * @throws RecommenderBuildException
     */
    public LenskitRecommender buildRecommender(LenskitConfiguration defaults) throws RecommenderBuildException {
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        if (defaults != null) {
            builder.addConfiguration(defaults);
        }
        builder.addConfiguration(config);
        return builder.build().createRecommender();
    }

    /**
     * Build a recommender graph (but don't instantiate any objects).
     *
     *
     * @param defaults Additional configuration.  This configuration comes <em>before</em> the
     *                 algorithm's configuration, so it is overridden if appropriate.
     * @return The recommender graph.
     * @throws RecommenderConfigurationException if there is an error configuring the recommender.
     */
    public DAGNode<Component,Dependency> buildRecommenderGraph(LenskitConfiguration defaults) throws RecommenderConfigurationException {
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        if (defaults != null) {
            builder.addConfiguration(defaults);
        }
        builder.addConfiguration(config);
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        rgb.addConfiguration(defaults);
        rgb.addConfiguration(config);
        try {
            return rgb.buildGraph();
        } catch (SolverException e) {
            throw new RecommenderConfigurationException("error configuring recommender", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LenskitAlgorithm(")
          .append(getName())
          .append(")");
        if (!attributes.isEmpty()) {
            sb.append("[");
            Joiner.on(", ")
                  .withKeyValueSeparator("=")
                  .appendTo(sb, attributes);
            sb.append("]");
        }
        return sb.toString();
    }
}

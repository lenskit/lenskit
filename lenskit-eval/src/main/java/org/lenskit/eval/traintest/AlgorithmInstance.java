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
package org.lenskit.eval.traintest;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.*;
import org.grouplens.lenskit.inject.RecommenderGraphBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * An instance of a recommender algorithm to be trained and measured.
 */
public class AlgorithmInstance {
    private static final Logger logger = LoggerFactory.getLogger(AlgorithmInstance.class);
    @Nullable
    private final String algoName;
    @Nonnull
    private final List<LenskitConfiguration> configurations;
    @Nonnull
    private final Map<String, Object> attributes;

    /**
     * Construct a new algorithm instance.
     * @param name The algorithm name.
     * @param config The algorithm configuration.
     */
    public AlgorithmInstance(String name, LenskitConfiguration config) {
        this(name, ImmutableList.of(config),
             ImmutableMap.<String,Object>of());
    }

    /**
     * Construct a new algorithm instance.
     * @param name The algorithm name.
     * @param configs The algorithm configurations.
     * @param attrs The attributes for this algorithm instance.
     */
    public AlgorithmInstance(String name, List<LenskitConfiguration> configs, Map<String, Object> attrs) {
        algoName = name;
        configurations = ImmutableList.copyOf(configs);
        attributes = ImmutableMap.copyOf(attrs);
    }


    /**
     * Get the name of this algorithm.  This returns a short name which is
     * used to identify the algorithm or instance.
     *
     * @return The algorithm's name
     */
    public String getName() {
        return algoName;
    }

    /**
     * Get the attributes of this algorithm.  These attributes are used to distinguish between similar algorithms,
     * e.g. different configurations of the same basic algorithm.
     * @return The algorithm attributes.
     */
    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get the recommender configurations.
     * @return
     */
    @Nonnull
    public List<LenskitConfiguration> getConfigurations() {
        return configurations;
    }

    /**
     * Build a recommender.
     *
     * @param defaults Additional configuration.  This configuration comes <em>before</em> the
     *                 algorithm's configuration, so it is overridden if appropriate.  It is used
     *                 for providing things such as DAOs.
     * @return The instantiated recommender.
     * @throws RecommenderBuildException
     */
    public LenskitRecommender buildRecommender(@Nullable LenskitConfiguration defaults) throws RecommenderBuildException {
        LenskitRecommenderEngineBuilder builder = LenskitRecommenderEngine.newBuilder();
        if (defaults != null) {
            builder.addConfiguration(defaults);
        }
        for (LenskitConfiguration cfg: configurations) {
            builder.addConfiguration(cfg);
        }
        return builder.build().createRecommender();
    }

    /**
     * Build a recommender graph (but don't instantiate any objects).
     *
     * @param defaults Additional configuration.  This configuration comes <em>before</em> the
     *                 algorithm's configuration, so it is overridden if appropriate.  It is used
     *                 for providing things such as DAOs.
     * @return The recommender graph.
     * @throws RecommenderConfigurationException if there is an error configuring the recommender.
     */
    public DAGNode<Component,Dependency> buildRecommenderGraph(@Nullable LenskitConfiguration defaults) throws RecommenderConfigurationException {
        RecommenderGraphBuilder rgb = new RecommenderGraphBuilder();
        if (defaults != null) {
            rgb.addConfiguration(defaults);
        }
        for (LenskitConfiguration cfg: configurations) {
            rgb.addConfiguration(cfg);
        }
        try {
            return rgb.buildGraph();
        } catch (ResolutionException e) {
            throw new RecommenderConfigurationException("error configuring recommender", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Algorithm(")
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

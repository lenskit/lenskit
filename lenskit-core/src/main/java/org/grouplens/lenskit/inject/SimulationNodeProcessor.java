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

import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.Satisfactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Node processor that simulates instantiation.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class SimulationNodeProcessor implements NodeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(SimulationNodeProcessor.class);

    public DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node, @Nonnull DAGNode<Component, Dependency> original) {
        Component label = node.getLabel();
        if (!label.getSatisfaction().hasInstance()) {
            Satisfaction instanceSat = Satisfactions.nullOfType(label.getSatisfaction().getErasedType());
            Component newLbl = Component.create(instanceSat,
                                                label.getCachePolicy());
            // build new node with replacement label
            DAGNodeBuilder<Component,Dependency> bld = DAGNode.newBuilder(newLbl);
            // retain all non-transient edges
            for (DAGEdge<Component,Dependency> edge: node.getOutgoingEdges()) {
                if (!GraphtUtils.edgeIsTransient(edge)) {
                    bld.addEdge(edge.getTail(), edge.getLabel());
                }
            }
            DAGNode<Component,Dependency> repl = bld.build();
            logger.debug("simulating instantiation of {}", node);
            return repl;
        } else {
            return node;
        }
    }
}

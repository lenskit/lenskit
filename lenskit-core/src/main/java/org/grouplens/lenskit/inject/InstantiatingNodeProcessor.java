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
class InstantiatingNodeProcessor implements NodeProcessor {
    private static final Logger logger = LoggerFactory.getLogger(InstantiatingNodeProcessor.class);
    private final Function<DAGNode<Component, Dependency>, Object> instantiator;

    InstantiatingNodeProcessor(Function<DAGNode<Component,Dependency>,Object> inst) {
        instantiator = inst;
    }

    public DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node, @Nonnull DAGNode<Component, Dependency> original) {
        Component label = node.getLabel();
        Satisfaction satisfaction = label.getSatisfaction();
        if (satisfaction.hasInstance()) {
            return node;
        }
        Object obj = instantiator.apply(node);

        Satisfaction instanceSat;
        if (obj == null) {
            instanceSat = Satisfactions.nullOfType(satisfaction.getErasedType());
        } else {
            instanceSat = Satisfactions.instance(obj);
        }
        Component newLabel = Component.create(instanceSat, label.getCachePolicy());
        // build new node with replacement label
        DAGNodeBuilder<Component,Dependency> bld = DAGNode.newBuilder(newLabel);
        // retain all non-transient edges
        for (DAGEdge<Component, Dependency> edge: node.getOutgoingEdges()) {
            if (!GraphtUtils.edgeIsTransient(edge)) {
                bld.addEdge(edge.getTail(), edge.getLabel());
            }
        }
        return bld.build();
    }
}

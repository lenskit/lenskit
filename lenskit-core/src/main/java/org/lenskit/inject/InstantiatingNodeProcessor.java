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

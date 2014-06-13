/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
import org.grouplens.grapht.graph.DAGNode;

import javax.annotation.Nonnull;

/**
 * Process a node in a graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public interface NodeProcessor {
    /**
     * Process a graph node.
     * @param node The node to process.
     * @param original The original node.  Some graph processing operations will change nodes prior
     *                 to processing due to changing some of their dependent nodes.  In such
     *                 operations, this parameter will receive the original node, while {@code node}
     *                 is the current node.
     * @return The processed node.
     */
    @Nonnull
    DAGNode<Component, Dependency> processNode(@Nonnull DAGNode<Component, Dependency> node,
                                               @Nonnull DAGNode<Component, Dependency> original);
}

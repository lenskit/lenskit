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
package org.grouplens.lenskit.eval.graph;

import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;

/**
 * A GraphWriter writes one or more {@link org.grouplens.grapht.Graph Graphs}
 * stored in memory to a permanent representation.
 */
public interface GraphWriter {

    /**
     * Perform any preparations necessary to begin constructing the representation.
     */
    void start();

    /**
     * Add the entirety of a graph to the underlying representation.
     *
     * @param g    The graph to add.
     * @param root The root node of the graph.
     */
    void addGraph(String label, Graph g, Node root);

    /**
     * Complete the representation and perform any required cleanup.
     */
    void finish();

}

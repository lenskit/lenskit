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
     * @param g The graph to add.
     * @param root The root node of the graph.
     */
	void addGraph(String label, Graph g, Node root);	
			
	/**
	 * Complete the representation and perform any required cleanup.
	 */
	void finish();
	
}

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

import java.io.File;

import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.CommandException;

public class DumpGraphCommand extends AbstractCommand<File> {

	private AlgorithmInstance algorithm;
	private GraphWriter writer;
	private File output;

	
	public DumpGraphCommand() {
		this("dumpGraph");
	}
	
	public DumpGraphCommand(String name) {
		super(name);
	}
	
	public DumpGraphCommand setName(String name) {
		this.name = name;
		return this;
	}
	
	public DumpGraphCommand setAlgorithm(AlgorithmInstance algorithm) {
		this.algorithm = algorithm;
		return this;
	}
	
	public DumpGraphCommand setOutput(File f) {
		output = f;
		writer = new GraphVizWriter(output);
		return this;
	}
	
	public DumpGraphCommand setOutput(GraphWriter w) {
		writer = w;
		return this;
	}
	
	@Override
	public File call() throws CommandException {		
		Graph initial = algorithm.getFactory().getInitialGraph();
		Node root = initial.getNode(null);
		writer.start();
		writer.addGraph("Initial Graph", initial, root);
		Graph instantiated = algorithm.getFactory().getInstantiatedGraph();
		root = instantiated.getNode(null);
		writer.addGraph("Instantiated Graph", instantiated, root);
		writer.finish();
		return output;
	}
}

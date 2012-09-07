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

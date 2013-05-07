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
package org.grouplens.lenskit.eval.graph;

import com.google.common.io.Closer;
import com.google.common.io.Files;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.AbstractSatisfactionVisitor;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractCommand;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

public class DumpGraphCommand extends AbstractCommand<File> {
    private static final Logger logger = LoggerFactory.getLogger(DumpGraphCommand.class);

    private LenskitAlgorithmInstance algorithm;
    private File output;
    private PreferenceDomain domain = null;
    private Class<? extends DataAccessObject> daoType;


    public DumpGraphCommand() {
        this(null);
    }

    public DumpGraphCommand(String name) {
        super(name);
    }

    @Override
    public String getName() {
        if (name == null) {
            return algorithm.getName();
        } else {
            return name;
        }
    }

    public DumpGraphCommand setAlgorithm(LenskitAlgorithmInstance algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public DumpGraphCommand setOutput(File f) {
        output = f;
        return this;
    }

    public DumpGraphCommand setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    public DumpGraphCommand setDaoType(Class<? extends DataAccessObject> daoType) {
        this.daoType = daoType;
        return this;
    }

    @Override
    public File call() throws CommandException {
        if (output == null) {
            logger.error("no output file specified");
            throw new IllegalStateException("no graph output file specified");
        }
        LenskitRecommenderEngineFactory factory = algorithm.getFactory().clone();
        if (domain != null) {
            factory.bind(PreferenceDomain.class).to(domain);
        }
        logger.info("dumping graph {}", getName());
        Graph initial = factory.getInitialGraph(daoType);
        logger.debug("graph has {} nodes", initial.getNodes().size());
        Graph unshared = factory.simulateInstantiation(initial);
        logger.debug("unshared graph has {} nodes", unshared.getNodes().size());
        try {
            writeGraph(initial, unshared.getNodes(), output);
        } catch (IOException e) {
            throw new CommandException("error writing graph", e);
        }
        // TODO Support dumping the instantiated graph again
        return output;
    }

    private void writeGraph(Graph g, Set<Node> unshared, File file) throws IOException, CommandException {
        Files.createParentDirs(output);
        Closer close = Closer.create();
        try {
            FileWriter writer = close.register(new FileWriter(file));
            GraphWriter gw = close.register(new GraphWriter(writer));
            renderGraph(g, unshared, gw);
        } catch (Throwable th) {
            throw close.rethrow(th, CommandException.class);
        } finally {
            close.close();
        }
    }

    private void renderGraph(final Graph g, Set<Node> unshared, final GraphWriter gw) throws CommandException {
        // Handle the root node
        Node root = g.getNode(null);
        if (root == null) {
            throw new CommandException("no root node for graph");
        }
        GraphDumper dumper = new GraphDumper(g, unshared, gw);
        try {
            String rid = dumper.setRoot(root);

            for (Edge e: g.getOutgoingEdges(root)) {
                Node target = e.getTail();
                CachedSatisfaction csat = target.getLabel();
                assert csat != null;
                if (!satIsNull(csat.getSatisfaction())) {
                    String id = dumper.process(target);
                    gw.putEdge(new EdgeBuilder(rid, id)
                                       .set("arrowhead", "vee")
                                       .build());
                }
            }
            dumper.finish();
        } catch (IOException e) {
            throw new CommandException("error writing graph", e);
        }
    }

    private boolean satIsNull(Satisfaction sat) {
        return sat.visit(new AbstractSatisfactionVisitor<Boolean>(false) {
            @Override
            public Boolean visitNull() {
                return true;
            }
        });
    }

}

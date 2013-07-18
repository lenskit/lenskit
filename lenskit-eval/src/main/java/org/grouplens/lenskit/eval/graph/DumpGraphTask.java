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
import org.grouplens.grapht.util.Providers;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;
import org.grouplens.lenskit.core.RecommenderInstantiator;
import org.grouplens.lenskit.data.dao.EventCollectionDAO;
import org.grouplens.lenskit.data.dao.EventDAO;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractTask;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

/**
 * Command to dump a graph.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("unused")
public class DumpGraphTask extends AbstractTask<File> {
    private static final Logger logger = LoggerFactory.getLogger(DumpGraphTask.class);

    private LenskitAlgorithmInstance algorithm;
    private File output;
    private PreferenceDomain domain = null;

    public DumpGraphTask() {
        this(null);
    }

    public DumpGraphTask(String name) {
        super(name);
    }

    @Nonnull
    @Override
    public String getName() {
        String name = super.getName();
        if (name == null) {
            name = algorithm.getName();
            if (name == null) {
                name = "algorithm";
            }
        }
        return name;
    }

    public LenskitAlgorithmInstance getAlgorithm() {
        return algorithm;
    }

    public DumpGraphTask setAlgorithm(LenskitAlgorithmInstance algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public File getOutput() {
        return output;
    }

    public DumpGraphTask setOutput(File f) {
        output = f;
        return this;
    }

    public DumpGraphTask setOutput(String fn) {
        return setOutput(new File(fn));
    }

    public PreferenceDomain getDomain() {
        return domain;
    }

    public DumpGraphTask setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    @Override
    public File perform() throws TaskExecutionException {
        if (output == null) {
            logger.error("no output file specified");
            throw new IllegalStateException("no graph output file specified");
        }
        LenskitConfiguration config = algorithm.getConfig().copy();
        // FIXME This is an ugly DAO-type kludge
        config.bind(EventDAO.class).toProvider(Providers.<EventDAO>of(null, EventDAO.class));
        if (domain != null) {
            config.bind(PreferenceDomain.class).to(domain);
        }
        logger.info("dumping graph {}", getName());
        RecommenderInstantiator instantiator;
        try {
            instantiator = RecommenderInstantiator.forConfig(config);
        } catch (RecommenderConfigurationException e) {
            throw new TaskExecutionException("error resolving algorithm configuration", e);
        }
        Graph initial = instantiator.getGraph();
        logger.debug("graph has {} nodes", initial.getNodes().size());
        Graph unshared = instantiator.simulate();
        logger.debug("unshared graph has {} nodes", unshared.getNodes().size());
        try {
            writeGraph(initial, unshared.getNodes(), output);
        } catch (IOException e) {
            throw new TaskExecutionException("error writing graph", e);
        }
        // TODO Support dumping the instantiated graph again
        return output;
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void writeGraph(Graph g, Set<Node> unshared, File file) throws IOException, TaskExecutionException {
        Files.createParentDirs(output);
        Closer close = Closer.create();
        try {
            FileWriter writer = close.register(new FileWriter(file));
            GraphWriter gw = close.register(new GraphWriter(writer));
            renderGraph(g, unshared, gw);
        } catch (Throwable th) {
            throw close.rethrow(th, TaskExecutionException.class);
        } finally {
            close.close();
        }
    }

    private void renderGraph(final Graph g, Set<Node> unshared, final GraphWriter gw) throws TaskExecutionException {
        // Handle the root node
        Node root = g.getNode(null);
        if (root == null) {
            throw new TaskExecutionException("no root node for graph");
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
                    gw.putEdge(EdgeBuilder.create(rid, id)
                                          .set("arrowhead", "vee")
                                          .build());
                }
            }
            dumper.finish();
        } catch (IOException e) {
            throw new TaskExecutionException("error writing graph", e);
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

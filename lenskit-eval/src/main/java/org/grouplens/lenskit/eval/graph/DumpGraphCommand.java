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

import com.google.common.io.Files;
import org.grouplens.grapht.graph.Graph;
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

public class DumpGraphCommand extends AbstractCommand<File> {
    private static final Logger logger = LoggerFactory.getLogger(DumpGraphCommand.class);

    private LenskitAlgorithmInstance algorithm;
    private File output;
    private PreferenceDomain domain = null;
    private Class<? extends DataAccessObject> daoType;


    public DumpGraphCommand() {
        this("dumpGraph");
    }

    public DumpGraphCommand(String name) {
        super(name);
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
        Graph initial = factory.getInitialGraph(daoType);
        try {
            writeGraph(factory.getInitialGraph(daoType), output);
        } catch (IOException e) {
            throw new CommandException("error writing graph", e);
        }
        // TODO Support dumping the instantiated graph again
        return output;
    }

    public void writeGraph(Graph g, File file) throws IOException {
        GraphRepr repr = new GraphRepr(g);
        Files.createParentDirs(file);
        FileWriter writer = new FileWriter(file);
        try {
            Templates.graphTemplate.execute(repr, writer);
        } finally {
            writer.close();
        }
    }
}

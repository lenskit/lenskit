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
package org.grouplens.lenskit.cli;

import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name="train-model", help="train a recommender model")
public class TrainModel implements Command {
    private final Namespace options;
    private final ScriptEnvironment environment;
    private final InputData input;

    public TrainModel(Namespace opts) {
        options = opts;
        environment = new ScriptEnvironment(opts);
        input = new InputData(opts);
    }

    @Override
    public void execute() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void configureArguments(ArgumentParser parser) {
        ScriptEnvironment.configureArguments(parser);;
        InputData.configureArguments(parser);
        parser.addArgument("config")
              .metavar("CONFIG")
              .help("load algorithm configuration from CONFIG");
    }
}

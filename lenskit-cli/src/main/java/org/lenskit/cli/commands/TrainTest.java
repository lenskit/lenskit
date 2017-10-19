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
package org.lenskit.cli.commands;

import com.google.auto.service.AutoService;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.grapht.util.ClassLoaderContext;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.cli.Command;
import org.lenskit.cli.LenskitCommandException;
import org.lenskit.cli.util.ScriptEnvironment;
import org.lenskit.eval.traintest.EvaluationException;
import org.lenskit.eval.traintest.TrainTestExperiment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

@AutoService(Command.class)
public class TrainTest implements Command {
    private final Logger logger = LoggerFactory.getLogger(TrainTest.class);

    @Override
    public String getName() {
        return "train-test";
    }

    @Override
    public String getHelp() {
        return "Run a train-test evaluation of algorithms over data sets.";
    }

    @Override
    public void configureArguments(ArgumentParser parser) {
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("config_file")
              .metavar("CONFIG")
              .help("Load train-test configuration from CONFIG")
              .type(File.class)
              .required(true);
    }

    private File getSpecFile(Namespace options) {
        return options.get("config_file");
    }

    @Override
    public void execute(Namespace options) throws LenskitCommandException {
        ScriptEnvironment env = new ScriptEnvironment(options);
        File specFile = getSpecFile(options);
        logger.info("loading train-test configuration from {}", specFile);

        ClassLoader cl = env.getClassLoader();
        ClassLoaderContext ctx = ClassLoaders.pushContext(cl);

        try {
            TrainTestExperiment experiment = TrainTestExperiment.load(specFile.toPath());
            experiment.execute();
        } catch (IOException e) {
            throw new LenskitCommandException("could not load spec file", e);
        } catch (EvaluationException e) {
            throw new LenskitCommandException("error running LensKit experiment", e);
        } finally {
            ctx.pop();
        }

    }
}

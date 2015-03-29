/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import org.grouplens.lenskit.eval.EvalConfig;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.script.EvalScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Run an eval script.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@AutoService(Command.class)
public class Eval implements Command {
    private final Logger logger = LoggerFactory.getLogger(Eval.class);

    @Override
    public void configureArguments(ArgumentParser parser) {
        parser.description("Run a LensKit evaluation script.  By default, the script is " +
                           "taken from 'eval.groovy'; use -f to override");
        ScriptEnvironment.configureArguments(parser);
        parser.addArgument("-F", "--force")
              .action(Arguments.storeTrue())
              .help("force eval tasks to run");
        parser.addArgument("-j", "--thread-count")
              .type(Integer.class)
              .setDefault(1)
              .metavar("N")
              .help("use N threads");
        parser.addArgument("-f", "--file")
              .type(File.class)
              .setDefault(new File("eval.groovy"))
              .metavar("SCRIPT")
              .help("run eval SCRIPT");
        parser.addArgument("target")
              .metavar("TARGET")
              .nargs("*")
              .help("run TARGET");
    }

    @Override
    public String getHelp() {
        return "run an evaluation script";
    }

    @Override
    public String getName() {
        return "eval";
    }

    private Properties getProperties(Namespace opts, ScriptEnvironment env) {
        Properties ps = new Properties(System.getProperties());
        ps.putAll(env.getProperties());
        if (getForce(opts)) {
            ps.setProperty(EvalConfig.FORCE_PROPERTY, "true");
        }
        int nthreads = getThreadCount(opts);
        if (nthreads >= 0) {
            ps.setProperty(EvalConfig.THREAD_COUNT_PROPERTY,
                           Integer.toString(nthreads));
        }
        return ps;
    }

    @Override
    public void execute(Namespace options) throws IOException, TaskExecutionException {
        File file = getFile(options);
        if (!file.exists()) {
            logger.error("script file {} does not exist", file);
            throw new FileNotFoundException(file.toString());
        }
        logger.info("loading evaluation from {}", file);
        ScriptEnvironment environment = new ScriptEnvironment(options);

        EvalScriptEngine engine = new EvalScriptEngine(environment.getClassLoader(),
                                                       getProperties(options, environment));

        EvalProject project = engine.loadProject(file);
        if (getTargets(options).isEmpty()) {
            String dft = project.getDefaultTarget();
            if (dft != null) {
                project.executeTarget(dft);
            } else if (!project.getAntProject().getTargets().isEmpty()) {
                String targets = Joiner.on(", ")
                                       .join(project.getAntProject().getTargets().keySet());
                logger.error("No targets specified and no default provided (try one of: {})",
                             targets);
                System.exit(2);
            }
        } else {
            project.executeTargets(getTargets(options));
        }
    }

    private boolean getForce(Namespace options) {
        return options.getBoolean("force");
    }

    private int getThreadCount(Namespace options) {
        return options.getInt("thread_count");
    }

    private File getFile(Namespace options) {
        return options.get("file");
    }

    private List<String> getTargets(Namespace options) {
        return options.get("target");
    }
}

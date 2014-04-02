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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.grouplens.lenskit.eval.EvalConfig;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.script.EvalScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

/**
 * Run an eval script.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@CommandSpec(name ="eval", help="run an evaluation script")
public class Eval implements Command {
    private final Logger logger = LoggerFactory.getLogger(Eval.class);

    public static void configureArguments(Subparser parser) {
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

    private final Namespace options;
    private final ScriptEnvironment environment;

    public Eval(Namespace opts) {
        options = opts;
        environment = new ScriptEnvironment(opts);
    }

    private Properties getProperties() {
        Properties ps = new Properties(System.getProperties());
        ps.putAll(environment.getProperties());
        if (getForce()) {
            ps.setProperty(EvalConfig.FORCE_PROPERTY, "true");
        }
        int nthreads = getThreadCount();
        if (nthreads >= 0) {
            ps.setProperty(EvalConfig.THREAD_COUNT_PROPERTY,
                           Integer.toString(nthreads));
        }
        return ps;
    }

    @Override
    public void execute() throws IOException, TaskExecutionException {
        System.out.println(options);
        File file = getFile();
        if (!file.exists()) {
            logger.error("script file {} does not exist", file);
            throw new FileNotFoundException(file.toString());
        }
        logger.info("loading evaluation from {}", file);

        EvalScriptEngine engine = new EvalScriptEngine(environment.getClassLoader(),
                                                       getProperties());

        EvalProject project = engine.loadProject(file);
        if (getTargets().isEmpty()) {
            String dft = project.getDefaultTarget();
            if (dft != null) {
                project.executeTarget(dft);
            } else if (!project.getAntProject().getTargets().isEmpty()) {
                String targets = Joiner.on(", ")
                                       .join(Iterables.transform(
                                               project.getAntProject().getTargets().keySet(),
                                               new Function() {
                                                   @Nullable
                                                   @Override
                                                   public Object apply(@Nullable Object input) {
                                                       return input == null ? null : input;
                                                   }
                                               }));
                logger.error("No targets specified and no default provided (try one of: {})",
                             targets);
                System.exit(2);
            }
        } else {
            project.executeTargets(getTargets());
        }
    }

    public boolean getForce() {
        return options.getBoolean("force");
    }

    public int getThreadCount() {
        return options.getInt("thread_count");
    }

    public File getFile() {
        return options.get("file");
    }

    public List<String> getTargets() {
        return options.get("target");
    }
}

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
package org.grouplens.lenskit.eval.cli;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import org.apache.tools.ant.Target;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.script.EvalScriptEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Main entry point to run the evaluator from the command line
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public class EvalCLI {
    private static final Logger logger = LoggerFactory.getLogger(EvalCLI.class);

    /**
     * Run the evaluator from the command line.
     *
     * @param args The command line arguments to the evaluator.
     */
    public static void main(String[] args) {
        EvalCLIOptions options = EvalCLIOptions.parse(args);
        EvalCLI cli = new EvalCLI(options);
        cli.run();
    }

    private final EvalCLIOptions options;

    public EvalCLI(EvalCLIOptions opts) {
        options = opts;
    }

    public void run() {
        ClassLoader loader = options.getClassLoader();
        EvalScriptEngine engine = new EvalScriptEngine(loader, options.getProperties());

        File f = options.getScriptFile();
        if (!f.exists()) {
            logger.error("script file {} does not exist", f);
            System.err.format("%s: file does not exist\n", f);
            System.err.println("lenskit-eval requires an eval script to execute (default: eval.groovy)");
            System.err.println("run lenskit-eval --help for more information");
            System.exit(1);
        }
        logger.info("loading evaluation from {}", f);
        try {
            EvalProject project = engine.loadProject(f);
            if (options.getArgs().length == 0) {
                String dft = project.getDefaultTarget();
                if (dft == null && !project.getAntProject().getTargets().isEmpty()) {
                    String targets = Joiner.on(", ")
                                           .join(Iterables.transform(
                                                   project.getAntProject().getTargets().keySet(),
                                                   new Function() {
                                                       @Nullable
                                                       @Override
                                                       public Object apply(@Nullable Object input) {
                                                           return input == null ? null : ((Target) input).getName();
                                                       }
                                                   }));
                    logger.error("No targets specified and no default provided (try one of {})",
                                 targets);
                    System.exit(2);
                }
            } else {
                project.executeTargets(options.getArgs());
            }
        } catch (TaskExecutionException e) {
            // we handle these specially
            reportError(e.getCause(), "%s: %s", f.getPath(), e.getMessage());
        } catch (IOException e) {
            reportError(e, "%s: %s", f.getPath(), e.getMessage());
        }
    }

    protected void reportError(@Nullable Throwable e, String msg, Object... args) {
        String text = String.format(msg, args);
        System.err.println(text);
        if (e instanceof ExecutionException) {
            e = e.getCause();
        }
        if (options.throwErrors()) {
            throw new RuntimeException(text, e);
        } else {
            if (e != null) {
                //noinspection ThrowableResultOfMethodCallIgnored
                StackTraceUtils.sanitize(e).printStackTrace(System.err);
            }
            System.exit(2);
        }
    }
}

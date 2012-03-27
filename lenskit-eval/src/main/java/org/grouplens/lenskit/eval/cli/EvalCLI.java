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
package org.grouplens.lenskit.eval.cli;

import org.codehaus.groovy.runtime.StackTraceUtils;
import org.grouplens.lenskit.eval.*;
import org.grouplens.lenskit.eval.config.EvalConfigEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main entry point to run the evaluator from the command line
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class EvalCLI {
    private static final Logger logger = LoggerFactory.getLogger(EvalCLI.class);
    
    /**
     * Run the evaluator from the command line.
     * @param args The command line arguments to the evaluator.
     */
    public static void main(String[] args) {
        EvalCLIOptions options = EvalCLIOptions.parse(args);
        EvalCLI cli = new EvalCLI(options);
        cli.run();
    }
    
    EvalCLIOptions options;
    
    public EvalCLI(EvalCLIOptions opts) {
        options = opts;
    }

    public void run() {
        ClassLoader loader = options.getClassLoader();
        EvalConfigEngine config = new EvalConfigEngine(loader);

        EvalOptions taskOptions = options.getEvalOptions();
        EvalTaskRunner runner = new EvalTaskRunner(taskOptions);

        File f = options.getConfigFile();
        logger.info("loading evaluation from {}", f);
        EvalEnvironment env;
        try {
            env = config.load(f);
        } catch (EvaluatorConfigurationException e) {
            // we handle these specially
            reportError(e.getCause(), "%s: %s", f.getPath(), e.getMessage());
            return;
        } catch (IOException e) {
            reportError(e, "%s: %s", f.getPath(), e.getMessage());
            return;
        }
        logger.info("loaded {} tasks", env.getTasks().size());

        List<String> taskNames = options.getTasks();
        List<EvalTask> toRun;
        if (taskNames.isEmpty()) {
            EvalTask task = env.getDefaultTask();
            if (task == null) {
                reportError(null, "%s: no default task", f);
                return;
            } else {
                toRun = Collections.singletonList(env.getDefaultTask());
            }
        } else {
            toRun = new ArrayList<EvalTask>(taskNames.size());
            for (String n: taskNames) {
                EvalTask t = env.getTask(n);
                if (t == null) {
                    reportError(null, "%s: no task named %s", f, n);
                } else {
                    toRun.add(t);
                }
            }
        }
        
        for (EvalTask task: toRun) {
            try{
                runner.execute(task);
            } catch (EvalTaskFailedException e) {
                reportError(e.getCause(), "Execution error: " + e.getMessage());
            }
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

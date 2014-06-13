/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.script;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.*;
import groovy.lang.*;
import groovy.util.AntBuilder;
import org.apache.commons.lang3.builder.Builder;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.grouplens.lenskit.config.GroovyUtils;
import org.grouplens.lenskit.eval.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Base class for evaluator configuration scripts. It contains the metaclass
 * machinery to set up evaluation taskMap.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class EvalScript extends Script implements GroovyObject {
    public final Logger logger = LoggerFactory.getLogger(getClass());
    private EvalScriptEngine engine;
    private ConfigMethodInvoker helper;
    private EvalProject project;
    private Target currentTarget;
    private AntBuilder ant;

    public EvalScript() {}

    //region Properties
    public EvalConfig getConfig() {
        return project.getConfig();
    }

    /**
     * Get the engine in use for this script.
     * @return The script engine for this script.
     */
    public EvalScriptEngine getEngine() {
        Preconditions.checkState(engine != null, "no script engine configured");
        return engine;
    }

    /**
     * Set the engine this script should use.
     * @param ece The engine to be used by this script.
     */
    public void setEngine(EvalScriptEngine ece) {
        engine = ece;
        if (project != null) {
            helper = new ConfigMethodInvoker(ece, project);
        }
    }

    /**
     * Get the eval project configured by this script.
     * @return The eval project.
     * @throws IllegalStateException if no eval project has been configured.
     * @see #setProject(EvalProject)
     */
    public EvalProject getProject() {
        Preconditions.checkState(project != null, "no project configured");
        return project;
    }

    /**
     * Set the eval project to be configured by this script.
     * @param prj The eval project to configure.
     */
    public void setProject(EvalProject prj) {
        project = prj;
        ant = new LenskitAntBuilder(getAntProject());
        if (engine != null) {
            helper = new ConfigMethodInvoker(engine, project);
        }
    }

    /**
     * Get the evaluator script. Provides global access to evaluator properties (such as the config).
     *
     * @return The eval script.
     */
    public EvalScript getEval() {
        return this;
    }

    /**
     * Get the Ant project.
     */
    public Project getAntProject() {
        return getProject().getAntProject();
    }

    /**
     * Get the Ant builder.
     */
    public AntBuilder getAnt() {
        Preconditions.checkState(ant != null, "no project configured");
        return ant;
    }

    /**
     * Invoke an Ant block.
     */
    public Task ant(Closure<?> block) {
        return (Task) getAnt().invokeMethod("sequential", block);
    }
    //endregion

    //region Utilities
    /**
     * Evaluate another script.
     * @param file The script to evaluate.
     * @return The return value of the script (typically the return value of its last expression).
     */
    public Object evalScript(File file) throws IOException, TaskExecutionException {
        return engine.runScript(file, project);
    }

    /**
     * Evaluate another script.
     * @param fn The script to evaluate.
     * @return The return value of the script (typically the return value of its last expression).
     */
    public Object evalScript(String fn) throws IOException, TaskExecutionException {
        return evalScript(new File(fn));
    }

    /**
     * Performs a file search based upon the parameter glob pattern.
     *
     * @param globPattern String in glob syntax giving the glob to expand.
     * @return A list of paths from the working directory to matching file names.
     */
    public List<String> glob(String globPattern) {
        return glob(globPattern, ".");
    }

    /**
     * Performs a file search based upon the parameter glob pattern.
     *
     * @param globPattern String in glob syntax giving the glob to expand.
     * @param baseDir     The base directory from which to search.
     * @return A list of paths from the base directory matching the glob.
     */
    public List<String> glob(String globPattern, String baseDir) {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(new String[]{globPattern});
        ds.setBasedir(baseDir);
        ds.scan();
        return ImmutableList.copyOf(ds.getIncludedFiles());
    }
    //endregion

    public EvalTarget target(String name,
                             @DelegatesTo(value=TargetDelegate.class,
                                          strategy=Closure.DELEGATE_FIRST) Closure<?> closure) {
        if (currentTarget != null) {
            throw new IllegalStateException("cannot nest targets");
        }

        EvalTarget target = new EvalTarget();
        target.setName(name);
        target.setProject(getAntProject());
        TargetDelegate delegate = new TargetDelegate(target);
        currentTarget = target;
        try {
            GroovyUtils.callWithDelegate(closure, delegate);
        } finally {
            currentTarget = null;
        }
        getAntProject().addTarget(target);
        return target;
    }

    public void defaultTarget(String name) {
        project.setDefaultTarget(name);
    }

    public void defaultTarget(Target target) {
        if (target == null) {
            project.setDefaultTarget(null);
        } else {
            project.setDefaultTarget(target.getName());
        }
    }

    //region Plumbing
    public Object methodMissing(String name, Object arg) {
        Object[] args = InvokerHelper.asArray(arg);
        logger.debug("searching for eval command {}", name);
        Object obj = null;
        try {
            obj = helper.callExternalMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new MissingMethodException(name, getClass(), args, true);
        }
        if (obj instanceof Builder) {
            return helper.finishBuilder((Builder<?>) obj);
        } else if (obj instanceof EvalTask) {
            final EvalTask<?> task = (EvalTask<?>) obj;
            if (currentTarget == null) {
                try {
                    ListenableFuture<List<Object>> deps = Futures.allAsList(helper.getDeps(task));
                    helper.clearDeps(task);
                    Runnable execute = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                task.execute();
                            } catch (TaskExecutionException e) {
                                throw new RuntimeException("task failure", e);
                            }
                        }
                    };
                    deps.addListener(execute, MoreExecutors.sameThreadExecutor());
                    if (task.isDone()) {
                        return Uninterruptibles.getUninterruptibly(task);
                    } else {
                        return task;
                    }
                } catch (ExecutionException e) {
                    throw new RuntimeException("task failure", e);
                }
            } else {
                EvalAntTask aTask = new EvalAntTask(task, helper.getDeps(task));
                aTask.setProject(getAntProject());
                aTask.setOwningTarget(currentTarget);
                aTask.init();
                currentTarget.addTask(aTask);
                return obj;
            }
        } else {
            return obj;
        }
    }

    @Override
    public Object run() {
        throw new UnsupportedOperationException("script not implemented");
    }
    //endregion
}

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
package org.grouplens.lenskit.eval.maven;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.Target;
import org.codehaus.groovy.runtime.StackTraceUtils;
import org.codehaus.plexus.util.DirectoryScanner;
import org.grouplens.lenskit.eval.TaskExecutionException;
import org.grouplens.lenskit.eval.EvalConfig;
import org.grouplens.lenskit.eval.EvalProject;
import org.grouplens.lenskit.eval.script.EvalScriptEngine;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Run a LensKit evaluation script.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Mojo(name = "run-eval",
      requiresDependencyResolution = ResolutionScope.RUNTIME,
      threadSafe = true)
// This @Execute should not really be needed, since this MOJO is designed for the
// LensKit lifecycle ... BUT by inserting this MOJO into the traditional lifecycle
// also we get the benefit that the Java files in the src tree get built before
// the LensKit lifecycle runs.
@Execute(lifecycle = "",
         phase = LifecyclePhase.PACKAGE)
public class EvalScriptMojo extends AbstractMojo {
    /**
     * The project.  Gives access to Maven state.
     */
    @Parameter(property="project",
               required=true,
               readonly=true)
    private MavenProject mavenProject;

    /**
     * The session.  Gives access to Maven session state.
     */
    @Parameter(property="session",
               required=true,
               readonly=true)
    private MavenSession mavenSession;

    /**
     * Name of recommender script. If none is given and scriptFiles
     * is not configured, default to use "eval.groovy"
     *
     */
    @Parameter(property = EvalConfig.EVAL_SCRIPT_PROPERTY,
                defaultValue = "eval.groovy")
    private String script;

    /**
     *  Pattern of the set of evaluation files. If this is configured,
     *  scriptFiles takes precedence over script.
     */
    @Parameter(property = EvalConfig.EVAL_SCRIPTFILES_PROPERTY)
    private FileSet scriptFiles;

    /**
     * Location of input data; any train-test sets will be placed
     * here, too.
     */
    @Parameter(property=EvalConfig.DATA_DIR_PROPERTY,
               defaultValue=".")
    private String dataDir;

    /**
     * Location of output data from the eval script.
     */
    @Parameter(property=EvalConfig.ANALYSIS_DIR_PROPERTY,
               defaultValue=".")
    private String analysisDir;

    /**
     * The number of evaluation threads to run.  A thread count of 0 uses as many threads
     * as there are available processors.
     */
    @Parameter(property=EvalConfig.THREAD_COUNT_PROPERTY)
    private int threadCount = 1;

    /**
     * Turn on to force eval steps to run.
     */
    @SuppressWarnings("FieldCanBeLocal")
    @Parameter(property=EvalConfig.FORCE_PROPERTY)
    private boolean force = false;

    /**
     * Skip running the evaluator. This parameter allows the evaluator to be skipped,
     * useful if you just want to re-run later phases (e.g. the analysis) without
     * re-running the entire evaluation.
     * To do that, run with <tt>mvn -Dlenskit.eval.skip=true</tt>.
     */
    @SuppressWarnings("FieldCanBeLocal")
    @Parameter(property = EvalConfig.SKIP_PROPERTY)
    private boolean skip = false;

    /**
     * Targets to run in the eval script.
     */
    @SuppressWarnings("FieldCanBeLocal")
    @Parameter(property = "lenskit.eval.targets")
    private List<String> targets = Lists.newArrayList();

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("skipping LensKit evaluation");
            return;
        }
        getLog().info("Running with thread count " + threadCount);
        getLog().info("The data directory is " + dataDir);
        getLog().info("The analysis directory is " + analysisDir);

        MavenLogAppender.setLog(getLog());
        try {
            doExecute();
        } finally {
            MavenLogAppender.removeLog();
        }
    }

    private void doExecute() throws MojoExecutionException {
        // We put these properties into the new properties object in
        // reverse order of importance.  The later properties override
        // the earlier ones.  First the system properties.  Then the
        // maven project properties.  Then the maven session user
        // properties.  These last are the -D definitions from the
        // command-line, which can thus override any other property
        // for this evaluation.  Then the special properties for this
        // MOJO, which can be set as arguments in the .pom file.  If
        // they are set in the .pom file, according to Maven logic
        // they should override any other attempt to set them, so they
        // come last.
        Properties properties = new Properties();
        properties.putAll(System.getProperties());
        properties.putAll(mavenProject.getProperties());
        properties.putAll(mavenSession.getProjectBuildingRequest().getUserProperties());
        properties.setProperty(EvalConfig.DATA_DIR_PROPERTY, dataDir);
        properties.setProperty(EvalConfig.ANALYSIS_DIR_PROPERTY, analysisDir);
        properties.setProperty(EvalConfig.THREAD_COUNT_PROPERTY,
                               Integer.toString(threadCount));
        properties.setProperty(EvalConfig.FORCE_PROPERTY,
                               Boolean.toString(force));

        if (getLog().isDebugEnabled()) {
            properties.list(System.out);
            mavenProject.getProperties().list(System.out);
            mavenSession.getProjectBuildingRequest().getUserProperties().list(System.out);
            System.getProperties().list(System.out);
        }

        ClassLoader loader = makeClassLoader();
        if (getLog().isDebugEnabled()) dumpClassLoader(loader);
        EvalScriptEngine engine = new EvalScriptEngine(loader, properties);

        List<File> files = new ArrayList<File>();
        try {
            files.addAll(getFiles(scriptFiles));
        } catch (IOException e) {
            getLog().error("Error reading in scriptFiles");
        } catch (NullPointerException e) {
            getLog().debug("no scriptFiles");
        }
        if(files.isEmpty()) {
            files.add(new File(script));
        }
        for(File f:files) {
            try {
                getLog().info("Loading evalution script from " + f.getPath());
                EvalProject project = engine.loadProject(f);
                List<String> toRun = targets;
                if (toRun == null) {
                    toRun = Lists.newArrayList();
                }
                if (toRun.isEmpty() && project.getDefaultTarget() != null) {
                    toRun.add(project.getDefaultTarget());
                }
                if (toRun.isEmpty() && !project.getAntProject().getTargets().isEmpty()) {
                    getLog().error("no target specified");
                    StringBuilder bld = new StringBuilder("available targets are: ");
                    Joiner.on(", ")
                          .appendTo(bld, Iterables.transform(
                                  project.getAntProject().getTargets().keySet(),
                                  new Function() {
                                      @Nullable
                                      @Override
                                      public Object apply(@Nullable Object input) {
                                          return input == null ? null : ((Target) input).getName();
                                      }
                                  }));
                    getLog().info(bld.toString());
                    throw new MojoExecutionException("no target specified");
                }
                project.executeTargets(toRun);
            } catch (TaskExecutionException e) {
                Throwable report = StackTraceUtils.deepSanitize(e).getCause();
                if (report == null) {
                    report = e;
                }
                getLog().error("Evaluation script failed:", report);
                throw new MojoExecutionException("An error occurred running the evaluation script", e);
            } catch (IOException e) {
                getLog().error(script + ": IO error", e);
                throw new MojoExecutionException("IO Exception on script", e);
            }
        }
    }

    /**
     *  Get the list of file  from the scriptFiles configuration
     * @param fileSet Configuration of file set
     * @return  list of included files
     * @throws IOException
     */
    private List<File> getFiles(FileSet fileSet) throws IOException {
        DirectoryScanner scanner = new DirectoryScanner();
        File baseDir = new File(fileSet.getDirectory());
        scanner.setBasedir(baseDir);
        List<String> includes = fileSet.getIncludes();
        List<String> excludes = fileSet.getExcludes();
        scanner.setIncludes(includes.toArray(new String[includes.size()]));
        scanner.setExcludes(excludes.toArray(new String[excludes.size()]));
        scanner.setCaseSensitive(true);
        scanner.scan();
        List<File> result = new ArrayList<File>();
        String[] files = scanner.getIncludedFiles();
        for(String f: files) {
            result.add(new File(baseDir, f));
        }
        return result;
    }

    /**
     * Compute the class loader we need in order to run. This is the class's class loader,
     * with the project dependencies added.
     * @return The class loader.
     */
    private ClassLoader makeClassLoader() throws MojoExecutionException {
        final List<URL> urls = new ArrayList<URL>();
        try {
            for (String cp: mavenProject.getRuntimeClasspathElements()) {
                urls.add(new File(cp).toURI().toURL());
            }
        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("dependencies not resolved", e);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException("invalid classpath element", e);
        }
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                                  getClass().getClassLoader());
    }

    private void dumpClassLoader(ClassLoader loader) {
        getLog().debug("class loader: " + loader);
        if (loader instanceof URLClassLoader) {
            URLClassLoader urls = (URLClassLoader) loader;
            for (URL url: urls.getURLs()) {
                getLog().debug("  - " + url.toString());
            }
        }
        ClassLoader parent = loader.getParent();
        if (parent != null) {
            dumpClassLoader(parent);
        }
    }

}

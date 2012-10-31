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
package org.grouplens.lenskit.eval.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.grouplens.lenskit.eval.CommandException;
import org.grouplens.lenskit.eval.config.EvalConfig;
import org.grouplens.lenskit.eval.config.EvalScriptEngine;

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
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
@Mojo(name = "run-eval",
      requiresDependencyResolution = ResolutionScope.RUNTIME,
      threadSafe = true)
public class EvalScriptMojo extends AbstractMojo {
    /**
     * The project.  Gives access to Maven state.
     */
    @Parameter(property="project",
               required=true,
               readonly=true)
    @SuppressWarnings("unused")
    private MavenProject project;

    /**
     * Location of the output class directory for this project's
     * build.  Used to extend the ClassLoader so it can find the
     * compiled classes when the script is running.
     */
    @Parameter(property="project.build.outputDirectory")
    private File buildDirectory;

    /**
     * Name of recommender script.
     */
    @Parameter(property="lenskit.eval.script",
               defaultValue="eval.groovy")
    private String script;

    /**
     * Location of input data; any train-test sets will be placed
     * here, too.
     */
    @Parameter(property="lenskit.eval.dataDir",
               defaultValue=".")
    private String dataDir;

    /**
     * Location of output data from the eval script.
     */
    @Parameter(property="lenskit.eval.analysisDir",
               defaultValue=".")
    private String analysisDir;

    /**
     * The number of evaluation threads to run.
     */
    @Parameter(property="lenskit.eval.threadCount")
    private int threadCount = 1;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("Running with thread count " + threadCount);
        getLog().info("The data directory is " + dataDir);
        getLog().info("The analysis directory is " + analysisDir);
        getLog().info("The script is " + script);

        MavenLogAppender.setLog(getLog());
        try {
            doExecute();
        } finally {
            MavenLogAppender.removeLog();
        }
    }

    private void doExecute() throws MojoExecutionException {
        // Before we can run, we get a new class loader that is a copy
        // of our class loader, with the build directory added to it.
        // That way the scripts can use classes that are compiled into
        // the build directory.
        URL buildUrl;
        try {
            buildUrl = buildDirectory.toURI().toURL();
            getLog().info("build directory " + buildUrl.toString());
        } catch (MalformedURLException e1) {
            throw new MojoExecutionException("Cannot build URL for build directory");
        }
        ClassLoader loader = makeClassLoader();
        Properties properties = new Properties(project.getProperties());
        properties.setProperty("lenskit.eval.dataDir", dataDir);
        properties.setProperty("lenskit.eval.analysisDir", analysisDir);
        properties.setProperty(EvalConfig.THREAD_COUNT_PROPERTY,
                               Integer.toString(threadCount));
        dumpClassLoader(loader);
        EvalScriptEngine engine = new EvalScriptEngine(loader, properties);

        try {
            File f = new File(script);
            getLog().info("Loading evalution script from " + f.getPath());
            engine.execute(f);
        } catch (CommandException e) {
            throw new MojoExecutionException("Invalid evaluation script", e);
        } catch (IOException e) {
            throw new MojoExecutionException("IO Exception on script", e);
        }
    }

    /**
     * Compute the class loader we need in order to run. This is the class's class loader,
     * with the project dependencies added.
     * @return The class loader.
     */
    private ClassLoader makeClassLoader() throws MojoExecutionException {
        final List<URL> urls = new ArrayList<URL>();
        try {
            for (String cp: project.getRuntimeClasspathElements()) {
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

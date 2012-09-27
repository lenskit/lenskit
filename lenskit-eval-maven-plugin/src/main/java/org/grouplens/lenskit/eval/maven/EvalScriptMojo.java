/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.config.EvalConfigEngine;
import org.grouplens.lenskit.eval.CommandException;

import com.google.common.primitives.Longs;

/**
 * Do a train-test evaluation of a set of algorithms.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @goal run-eval
 * @requiresDependencyResolution runtime
 */
public class EvalScriptMojo extends AbstractMojo {
    /**
     * The project.  Gives access to Maven state.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
	private MavenProject project;
    
    /**
     * Location of the output class directory for this project's build.
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File buildDirectory;
    
    /**
     * Location of recommender scripts.
     * @parameter expression="${lenskit.eval.scriptDir} default="."
     */
    private String scriptDir;

    /**
     * Name of recommender script.
     * @parameter default="eval.groovy"
     */
    private String scriptName;

    /**
     * Location of input data; any train-test sets will be placed
     * here, too.
     * @parameter default="."
     */
    private String dataDir;

    /**
     * Location of output data from the eval script.
     * @parameter default="."
     */
    private String outputDir;

    /**
     * Set of recommender scripts.
     * @parameter default="{eval.groovy}"
     */
    private String[] scripts;
    
    /**
     * The number of evaluation threads to run.
     * @parameter expression="${lenskit.threadCount}"
     */
    private int threadCount = 1;

    public void execute() throws MojoExecutionException {
        getLog().info("Running with thread count " + threadCount);
        getLog().info("The scripts are " + Arrays.toString(scripts));
        getLog().info("The scripts directory is " + scriptDir);
        getLog().info("The data directory is " + dataDir);
        getLog().info("The output directory is " + outputDir);

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
        ClassLoader loader = new URLClassLoader(new URL[]{buildUrl},
						getClass().getClassLoader());
	Properties properties = new Properties(project.getProperties());
	properties.setProperty("lenskit.eval.dataDir", dataDir);
	properties.setProperty("lenskit.eval.outputDir", outputDir);
        EvalConfigEngine engine = new EvalConfigEngine(loader, properties);

        try {
	    File dir = new File(scriptDir);

	    for (String name: scripts) {
		File f = new File(dir, name);
		getLog().info("Loading evalution script from " + f.getPath());
		engine.execute(f);
	    }
	} catch (CommandException e) {
	    throw new MojoExecutionException("Invalid recommender", e);
	} catch (IOException e) {
	    throw new MojoExecutionException("IO Exception on script", e);
	}
    }            
    
}

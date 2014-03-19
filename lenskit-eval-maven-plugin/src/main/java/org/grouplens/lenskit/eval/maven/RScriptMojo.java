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

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.grouplens.lenskit.eval.EvalConfig;

import java.io.File;
import java.io.IOException;

import static org.apache.commons.exec.CommandLine.parse;
import static org.apache.commons.io.FileUtils.copyFile;

/**
 * Run an R script for statistical analysis.
 * 
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @deprecated Run R from within the eval script, using the Ant exec task instead.
 */
@Mojo(name = "run-r",
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        threadSafe = true)
@Deprecated
public class RScriptMojo extends AbstractMojo {
    /**
     * The project. Gives access to Maven state.
     */
    @Parameter(property = "project",
            required = true,
            readonly = true)
    private MavenProject mavenProject;

    /**
     * The session. Gives access to Maven session state.
     */
    @Parameter(property = "session",
            required = true,
            readonly = true)
    private MavenSession mavenSession;

    /**
     * Name of R script.
     * 
     */
    @Parameter(property = "lenskit.analyze.script",
            defaultValue = "chart.R")
    private String analysisScript;

    /**
     * Name or path to executable Rscript program.
     * 
     */
    @Parameter(property = "rscript.executable",
            defaultValue = "Rscript")
    private String rscriptExecutable;

    /**
     * Location of output data from the eval script.
     */
    @Parameter(property = EvalConfig.ANALYSIS_DIR_PROPERTY,
	       defaultValue = ".")
    private String analysisDir;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("The analysis directory is " + analysisDir);
        getLog().info("The analysisScript is " + analysisScript);
        getLog().info("The R executable is " + rscriptExecutable);
        
        // Copy the script file into the working directory.  We could just execute
        // it from its original location, but it will be easier for our users to 
        // have the copy from the src directory next to its results in target.
        File scriptFile = new File(analysisScript);
        File scriptCopy = new File(analysisDir, scriptFile.getName());
        try {
            if (! scriptFile.getCanonicalPath().equals(scriptCopy.getCanonicalPath())) {
                copyFile(scriptFile, scriptCopy);
            }
        } catch (IOException e1) {
            throw new MojoExecutionException("Unable to copy scriptFile " + scriptFile.getAbsolutePath()
                                             + " into working directory " + scriptCopy.getAbsolutePath());
        }

        // Generate the command line for executing R.
        final CommandLine command =
                new CommandLine(rscriptExecutable)
                        .addArgument(scriptCopy.getAbsolutePath(), false);
        getLog().debug("command: " + command);  

        // Execute the command line, in the working directory.
        final DefaultExecutor executor = new DefaultExecutor();
        executor.setWorkingDirectory(new File(analysisDir));
        try {
            if (executor.execute(command) != 0) {
                throw new MojoExecutionException( "Error code returned for: " + command.toString() );
            }
        } catch (ExecuteException e) {
            throw new MojoExecutionException("Error executing command: " + command.toString(), e);
        } catch (IOException e) {
            throw new MojoExecutionException("IO Exception while executing command: " + command.toString(), e);
        }
     }

}

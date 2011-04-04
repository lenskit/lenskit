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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.mappers.GlobPatternMapper;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @goal graph-algorithms
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 */
public class LenskitGraphAlgorithmsMojo extends AbstractMojo {
    /**
     * A <code>fileSet</code> rule to select the algorithm definition files.
     * @parameter expression="${lenskit.algorithmFiles}"
     */
    private FileSet algorithmFiles;
    
    /**
     * A single algorithm file to use.
     * @parameter expression="${lenskit.algorithmFile}"
     */
    private File algorithmFile;
    
    /**
     * Directory to store algorithm graphs.
     * @parameter expression="${lenskit.graph.directory}"
     *      default-value="${project.build.directory}/algo-graphs"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Location of the output class directory for this project's build.
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File classDirectory;
    
    private GlobPatternMapper jsMapper;
    
    public LenskitGraphAlgorithmsMojo() {
        jsMapper = new GlobPatternMapper();
        jsMapper.setFrom("*.js");
        jsMapper.setTo("*.dot");
    }

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        outputDirectory.mkdirs();
        URL outputUrl;
        try {
            outputUrl = classDirectory.toURI().toURL();
        } catch (MalformedURLException e1) {
            throw new MojoExecutionException("Cannot build URL for project output directory");
        }
        ClassLoader loader = new URLClassLoader(new URL[]{outputUrl}, getClass().getClassLoader());
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(loader);
        try {
            MavenLoggerFactory.getInstance().setLog(getLog());
            getLog().info("Loading file: " + algorithmFile.getPath());
            AlgorithmInstance algo = AlgorithmInstance.load(algorithmFile);
            
            String name = algorithmFile.getName();
            String tgtName = jsMapper.mapFileName(name);
            File outFile = new File(outputDirectory, tgtName);
            getLog().info("Graphing to file: " + outFile.getPath());
            
            PrintWriter output;
            try {
                output = new PrintWriter(outFile);
            } catch (FileNotFoundException e1) {
                throw new MojoFailureException("Cannot create output file", e1);
            }
            
            Injector graphInjector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
            GraphvizRenderer renderer = graphInjector.getInstance(GraphvizRenderer.class);
            renderer.setOut(output).setRankdir("TB");
            Provider<InjectorGrapher> gprovider = graphInjector.getProvider(InjectorGrapher.class);
            Injector injector = Guice.createInjector(new AbstractModule() {
                protected void configure() {
                }
                @SuppressWarnings("unused")
                @Provides public RatingDataAccessObject provideDataSource() {
                    throw new RuntimeException("No data source available");
                }
            }, algo.getModule());
            try {
                gprovider.get().of(injector).graph();
            } catch (IOException e) {
                throw new MojoFailureException("I/O error graphing algorithm", e);
            }
        } catch (InvalidRecommenderException e) {
            throw new MojoExecutionException("Invalid recommender", e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}

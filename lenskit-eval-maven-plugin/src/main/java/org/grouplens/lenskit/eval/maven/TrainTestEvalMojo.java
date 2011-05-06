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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.common.slf4j.maven.MavenLoggerFactory;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.holdout.TrainTestPredictEvaluator;
import org.grouplens.lenskit.eval.predict.CoverageEvaluator;
import org.grouplens.lenskit.eval.predict.MAEEvaluator;
import org.grouplens.lenskit.eval.predict.NDCGEvaluator;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.grouplens.lenskit.eval.predict.RMSEEvaluator;
import org.grouplens.lenskit.eval.results.MultiRunTableResultManager;
import org.grouplens.lenskit.eval.results.ResultAccumulator;

/**
 * Do a train-test evaluation of a set of algorithms.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @goal train-test
 * @requiresDependencyResolution runtime
 */
public class TrainTestEvalMojo extends AbstractMojo {
    /**
     * The project.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private MavenProject project;
    
    /**
     * Whether we are in interactive mode.
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    
    /**
     * The database driver.
     * @parameter expression="${lenskit.databaseDriver}"
     */
    private String databaseDriver;
    
    /**
     * The databases for evaluation.
     * @parameter expression="${lenskit.databases}"
     * @required
     */
    private FileSet databases;
    
    /**
     * Location of the output file.
     * 
     * @parameter expression="${lenskit.outputFile}" default-value="${project.build.directory}/lenskit.csv"
     * @required
     */
    private File outputFile;
    
    /**
     * Location of the output class directory for this project's build.
     * @parameter expression="${project.build.outputDirectory}"
     */
    private File classDirectory;
    
    /**
     * Location of the recommender configuration script.
     * @parameter expression="${lenskit.recommenderScript}"
     */
    private File recommenderScript;
    
    /**
     * Set of recommender scripts.
     * @parameter expression="${lenskit.recommenderScripts}"
     */
    private FileSet recommenderScripts;
    
    /**
     * The number of evaluation threads to run.
     * @parameter expression="${lenskit.threadCount}"
     */
    private int threadCount = 1;

    public void execute() throws MojoExecutionException {
        if (databaseDriver != null) {
            try {
                Class.forName(databaseDriver);
            } catch (ClassNotFoundException e) {
                throw new MojoExecutionException("Database driver " + databaseDriver + " not found");
            }
        }
        getLog().info("Running with thread count " + threadCount);
        // Before we can run, we need to replace our class loader to include
        // the project's output directory.  Kinda icky, but it's the brakes.
        // TODO: find a better way to set up our class loader
        if (recommenderScript == null && recommenderScripts == null)
            throw new MojoExecutionException("No recommender script(s) specified");
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
            MavenLoggerFactory.setLog(getLog());

            FileSetManager fsmgr = new FileSetManager();
            
            List<AlgorithmInstance> algorithms = new LinkedList<AlgorithmInstance>();
            try {
                if (recommenderScripts != null) {
                    getLog().debug("Loading multiple recommender scripts");
                    getLog().debug("Directory: " + recommenderScripts.getDirectory());
                    getLog().debug("Excludes: " + recommenderScripts.getExcludes());
                    getLog().debug("Includes: " + recommenderScripts.getIncludes());
                    String[] scriptNames = fsmgr.getIncludedFiles(recommenderScripts);
                    File base = new File(recommenderScripts.getDirectory());
                    for (String name: scriptNames) {
                        File scriptFile = new File(base, name);
                        getLog().info("Loading recommender from " + scriptFile.getPath());
                        algorithms.add(AlgorithmInstance.load(scriptFile, loader));                    
                    }
                }

                if (recommenderScript != null)
                    algorithms.add(AlgorithmInstance.load(recommenderScript, loader));
            } catch (InvalidRecommenderException e) {
                throw new MojoExecutionException("Invalid recommender", e);
            }
            
            List<PredictionEvaluator> evaluators = new ArrayList<PredictionEvaluator>();
            evaluators.add(new CoverageEvaluator());
            evaluators.add(new MAEEvaluator());
            evaluators.add(new RMSEEvaluator());
            evaluators.add(new NDCGEvaluator());
            
            MultiRunTableResultManager output =
                new MultiRunTableResultManager(algorithms, evaluators, outputFile);
            
            getLog().info(String.format("Evaluating recommenders in %d threads", threadCount));
            ExecutorService svc = Executors.newFixedThreadPool(threadCount);
            try {
                List<Future<?>> results = new ArrayList<Future<?>>();
                
                String[] dbNames = fsmgr.getIncludedFiles(databases);
                for (String db: dbNames) {
                    File dbf = new File(databases.getDirectory(), db);
                    String name = FileUtils.basename(db, ".db");
                    String dsn = "jdbc:sqlite:" + dbf.getPath();
                    Runnable task = new EvalTask(name, dsn, algorithms, output.makeAccumulator(name));
                    results.add(svc.submit(task));
                }
                if (svc != null) {
                    for (Future<?> f: results) {
                        boolean done = false;
                        while (!done) {
                            try {
                                f.get();
                                done = true;
                            } catch (InterruptedException e) {
                                /* no-op, try again */
                            } catch (ExecutionException e) {
                                Throwable base = e;
                                if (e.getCause() != null)
                                    base = e;
                                throw new MojoExecutionException("Error testing recommender", base);
                            }
                        }
                    }
                }
            } finally {
                svc.shutdown();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
    
    private boolean showProgress() {
        return getLog().isInfoEnabled() && settings.isInteractiveMode() && threadCount == 1;
    }
    
    class EvalTask implements Runnable {
        private String name;
        private String dsn;
        private List<AlgorithmInstance> algorithms;
        private ResultAccumulator accum;
        
        public EvalTask(String name, String dsn, List<AlgorithmInstance> algos, ResultAccumulator acc) {
            this.name = name;
            this.dsn = dsn;
            algorithms = algos;
            accum = acc;
        }
        public void run() {
            getLog().info("Running evaluation on " + name);
            getLog().debug("Opening database " + dsn);
            Connection dbc;
            try {
                dbc = DriverManager.getConnection(dsn);
            } catch (SQLException e) {
                throw new RuntimeException("Error opening database", e);
            }
            getLog().debug("Creating evaluator");
            TrainTestPredictEvaluator eval =
                new TrainTestPredictEvaluator(dbc, "train", "test");
            if (showProgress())
                eval.setProgressStream(System.out);
            getLog().debug("Evaluating algorithms");
            eval.evaluateAlgorithms(algorithms, accum);
        }
    }
}

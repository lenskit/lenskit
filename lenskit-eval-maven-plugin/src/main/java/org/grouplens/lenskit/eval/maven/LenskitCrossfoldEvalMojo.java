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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.dao.RatingCollectionDAO;
import org.grouplens.lenskit.data.dao.RatingDataAccessObject;
import org.grouplens.lenskit.data.dao.SimpleFileDAO;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.crossfold.CrossfoldEvaluator;
import org.grouplens.lenskit.eval.crossfold.RandomUserRatingProfileSplitter;
import org.grouplens.lenskit.eval.crossfold.TimestampUserRatingProfileSplitter;
import org.grouplens.lenskit.eval.crossfold.UserRatingProfileSplitter;
import org.grouplens.lenskit.eval.predict.CoverageEvaluator;
import org.grouplens.lenskit.eval.predict.MAEEvaluator;
import org.grouplens.lenskit.eval.predict.PredictionEvaluator;
import org.grouplens.lenskit.eval.predict.RMSEEvaluator;

/**
 * Run a crossfold evaluation with LensKit.
 * 
 * @goal crossfold-eval
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 */
public class LenskitCrossfoldEvalMojo extends AbstractMojo {
    /**
     * The project.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @SuppressWarnings("unused")
    private MavenProject project;
    
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
     * Input data location.
     * @parameter expression="${lenskit.dataFile}"
     * @required
     */
    private File dataFile;
    
    /**
     * Input file delimiter.
     * @parameter expression="${lenskit.inputDelimiter}"
     */
    private String inputDelimiter = "\t";
    
    /**
     * Split mode.
     * @parameter expression="${lenskit.splitMode}" default-value="random"
     */
    private String splitMode;
    
    /**
     * Fold count.
     * @parameter expression="${lenskit.numFolds}" default-value="5"
     */
    private int numFolds;
    
    /**
     * Preload the ratings?
     * @parameter expression="${lenskit.preload}" default-value="false"
     */
    private boolean preload;
    
    /**
     * Holdout fraction for test users.
     * @parameter expression="${lenskit.holdoutFraction}" default-value="0.333333"
     */
    private double holdoutFraction;

    public void execute() throws MojoExecutionException {
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
            MavenLoggerFactory.getInstance().setLog(getLog());
            UserRatingProfileSplitter splitter;
            if (splitMode.toLowerCase().equals("random"))
                splitter = new RandomUserRatingProfileSplitter(holdoutFraction);
            else if (splitMode.toLowerCase().equals("timestamp"))
                splitter = new TimestampUserRatingProfileSplitter(holdoutFraction);
            else
                throw new MojoExecutionException("Invalid split mode: " + splitMode);

            RatingDataAccessObject ratings;
            try {
                ratings = new SimpleFileDAO(dataFile, inputDelimiter);
                ratings.openSession();
            } catch (FileNotFoundException e1) {
                throw new MojoExecutionException("Input file " + dataFile + " not found", e1);
            }
            try {
                if (preload) {
                    RatingDataAccessObject source = ratings;
                    ArrayList<Rating> rlist = Cursors.makeList(ratings.getRatings());
                    rlist.trimToSize();
                    ratings = new RatingCollectionDAO(rlist);
                    source.closeSession();
                    ratings.openSession();
                }

                List<AlgorithmInstance> algorithms = new LinkedList<AlgorithmInstance>();
                try {
                    if (recommenderScripts != null) {
                        getLog().debug("Loading multiple recommender scripts");
                        getLog().debug("Directory: " + recommenderScripts.getDirectory());
                        getLog().debug("Excludes: " + recommenderScripts.getExcludes());
                        getLog().debug("Includes: " + recommenderScripts.getIncludes());
                        FileSetManager fsmgr = new FileSetManager();
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
                CrossfoldEvaluator eval;
                try {
                    eval = new CrossfoldEvaluator(ratings, algorithms, numFolds, splitter, null);
                } catch (IOException e) {
                    throw new MojoExecutionException("Error loading evaluator.", e);
                }
                List<PredictionEvaluator> evaluators = new ArrayList<PredictionEvaluator>();
                evaluators.add(new CoverageEvaluator());
                evaluators.add(new MAEEvaluator());
                evaluators.add(new RMSEEvaluator());

                try {
                    eval.run(evaluators, outputFile);
                } catch (Exception e) {
                    throw new MojoExecutionException("Unexpected failure running recommender evaluation.", e);
                }
            } finally {
                ratings.closeSession();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}

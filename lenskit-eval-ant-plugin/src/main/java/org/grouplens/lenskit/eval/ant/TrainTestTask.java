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
/**
 * 
 */
package org.grouplens.lenskit.eval.ant;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.holdout.TrainTestPredictEvaluator;
import org.grouplens.lenskit.eval.results.AlgorithmEvaluationRecipe;
import org.grouplens.lenskit.eval.results.ResultAccumulator;

import com.google.common.primitives.Longs;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TrainTestTask extends Task {
	private String databaseDriver = "org.sqlite.JDBC";
	private FileSet databases;
	private File outFile;
	private File script;
	private int threadCount = 1;
	private File predictionOutput;
	private Properties properties = new Properties();
	
	public void setDatabaseDriver(String driver) {
		databaseDriver = driver;
	}
	
	public void setOutput(File f) {
		outFile = f;
	}
	
	public void setScript(File s) {
	    script = s;
	}
	
	public void setThreadCount(int n) {
		threadCount = n;
	}
	
	public void setPredictions(File f) {
		predictionOutput = f;
	}
	
	public void addConfiguredDatabases(FileSet dbs) {
		databases = dbs;
	}
	
	public void addConfiguredProperty(Property prop) {
	    properties.put(prop.getName(), prop.getValue());
	}
	
	public void execute() throws BuildException {
		if (databaseDriver != null) {
			try {
				Class.forName(databaseDriver);
			} catch (ClassNotFoundException e) {
				throw new BuildException("Database driver " + databaseDriver + " not found");
			}
		}
		log("Running with thread count " + threadCount);
		AlgorithmEvaluationRecipe recipe;
		try {
		    log("Loading recommender from " + script.getPath());
		    recipe = AlgorithmEvaluationRecipe.load(script, properties, outFile);
		    if (predictionOutput != null) {
		        try {
		            recipe.setPredictionOutput(predictionOutput);
		        } catch (IOException e) {
		            handleErrorOutput("Cannot open prediction output");
		        }
		    }
		} catch (InvalidRecommenderException e) {
			throw new BuildException("Invalid recommender", e);
		}

		log(String.format("Evaluating recommenders in %d threads", threadCount));
		ExecutorService svc = Executors.newFixedThreadPool(threadCount);
		try {
			List<Future<?>> results = new ArrayList<Future<?>>();
			DirectoryScanner dbs = databases.getDirectoryScanner();
			dbs.scan();
			String[] dbNames = dbs.getIncludedFiles();
			File[] dbFiles = new File[dbNames.length];
			for (int i = 0; i < dbNames.length; i++) {
				dbFiles[i] = new File(dbs.getBasedir(), dbNames[i]);
			}
			Arrays.sort(dbFiles, new Comparator<File>() {
				public int compare(File f1, File f2) {
					return Longs.compare(f1.length(), f2.length());
				}
			});
			for (int i = 0; i < dbFiles.length; i++) {
				File dbf = dbFiles[i];
				String name = FileUtils.basename(dbf.getName(), ".db");
				String dsn = "jdbc:sqlite:" + dbf.getPath();
				Runnable task = new EvalTask(name, dsn, recipe.getAlgorithms(),
				    recipe.makeAccumulator(name));
				results.add(svc.submit(task));
			}

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
						throw new BuildException("Error testing recommender", base);
					}
				}
			}
		} finally {
			svc.shutdown();
		}       
	}
	
	protected boolean showProgress() {
		return false;
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
            log("Running evaluation on " + name);
            log("Opening database " + dsn, Project.MSG_DEBUG);
            Connection dbc;
            try {
                dbc = DriverManager.getConnection(dsn);
            } catch (SQLException e) {
                throw new RuntimeException("Error opening database", e);
            } 
            try {
                log("Creating evaluator", Project.MSG_DEBUG);
                TrainTestPredictEvaluator eval =
                    new TrainTestPredictEvaluator(dbc, "train", "test");
                if (showProgress())
                    eval.setProgressStream(System.out);
                log("Evaluating algorithms", Project.MSG_DEBUG);
                eval.evaluateAlgorithms(algorithms, accum);
            } finally {
                try {
                    dbc.close();
                } catch (SQLException e) {
                    handleErrorFlush("Error closing DB: " + e.getMessage());
                }
            }
        }
    }
}

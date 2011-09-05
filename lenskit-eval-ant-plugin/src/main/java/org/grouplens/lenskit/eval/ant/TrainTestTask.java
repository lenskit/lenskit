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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.codehaus.plexus.util.FileUtils;
import org.grouplens.lenskit.eval.InvalidRecommenderException;
import org.grouplens.lenskit.eval.traintest.EvaluationRecipe;
import org.grouplens.lenskit.eval.traintest.TrainTestPredictEvaluator;
import org.grouplens.lenskit.util.parallel.ExecHelpers;

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
    private int threadCount = Runtime.getRuntime().availableProcessors();
    private boolean isolateDatasets = false;
    private File predictionOutput;
    private boolean useTimestamp = true;
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
        if (n > 0)
            threadCount = n;
        else
            threadCount = Runtime.getRuntime().availableProcessors();
    }

    public void setIsolate(boolean isolate) {
        isolateDatasets = isolate;
    }

    public void setPredictions(File f) {
        predictionOutput = f;
    }

    public void setTimestamp(boolean ts) {
        useTimestamp = ts;
    }

    public void addConfiguredDatabases(FileSet dbs) {
        databases = dbs;
    }

    public void addConfiguredProperty(Property prop) {
        properties.put(prop.getName(), prop.getValue());
    }

    @Override
    public void execute() throws BuildException {
        if (databaseDriver != null) {
            try {
                Class.forName(databaseDriver);
            } catch (ClassNotFoundException e) {
                throw new BuildException("Database driver " + databaseDriver + " not found");
            }
        }
        EvaluationRecipe recipe;
        try {
            log("Loading recommender from " + script.getPath());
            recipe = EvaluationRecipe.load(script, properties, outFile);
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

        DirectoryScanner dbs = databases.getDirectoryScanner();
        dbs.scan();
        String[] dbNames = dbs.getIncludedFiles();
        File[] dbFiles = new File[dbNames.length];
        for (int i = 0; i < dbNames.length; i++) {
            dbFiles[i] = new File(dbs.getBasedir(), dbNames[i]);
        }
        Arrays.sort(dbFiles, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return Longs.compare(f1.length(), f2.length());
            }
        });

        List<TrainTestPredictEvaluator> evaluators =
            new ArrayList<TrainTestPredictEvaluator>(dbFiles.length);
        for (int i = 0; i < dbFiles.length; i++) {
            File dbf = dbFiles[i];
            String name = FileUtils.basename(dbf.getName(), ".db");
            String dsn = "jdbc:sqlite:" + dbf.getPath();
            TrainTestPredictEvaluator eval = new TrainTestPredictEvaluator(dsn, "train", "test");
            eval.setName(name);
            eval.setTimestampEnabled(useTimestamp);
            evaluators.add(eval);
        }

        if (isolateDatasets) {
            for (TrainTestPredictEvaluator eval: evaluators) {
                eval.runEvaluation(recipe);
            }
        } else {
            ExecutorService svc = Executors.newFixedThreadPool(threadCount);
            try {
                List<Future<?>> results = new ArrayList<Future<?>>();
                for (TrainTestPredictEvaluator eval: evaluators) {
                    Collection<Runnable> tasks = eval.makeEvalTasks(recipe);
                    for (Runnable task: tasks) {
                        results.add(svc.submit(task));
                    }
                }

                try {
                    ExecHelpers.waitAll(results);
                } catch (ExecutionException e) {
                    throw new BuildException(e);
                }
            } finally {
                svc.shutdown();
            }
        }
    }
}

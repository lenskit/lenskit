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
package org.grouplens.lenskit.eval;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.io.Closer;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.RecommenderBuildException;
import org.grouplens.lenskit.core.LenskitRecommender;
import org.grouplens.lenskit.eval.algorithm.LenskitAlgorithmInstance;
import org.grouplens.lenskit.eval.data.DataSource;
import org.grouplens.lenskit.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Train a recommender algorithm and process it with a function.
 */
public class TrainModelTask<T> extends AbstractTask<T> {
    private static final Logger logger = LoggerFactory.getLogger(TrainModelTask.class);

    private LenskitAlgorithmInstance algorithm;
    private File writeFile;
    private DataSource inputData;
    private Function<LenskitRecommender, T> action;

    public TrainModelTask() {
        super("train-model");
    }

    public TrainModelTask(String name) {
        super(name);
    }

    public LenskitAlgorithmInstance getAlgorithm() {
        return algorithm;
    }

    public File getWriteFile() {
        return writeFile;
    }

    public DataSource getInputData() {
        return inputData;
    }

    public Function<LenskitRecommender, T> getAction() {
        return action;
    }

    /**
     * Configure the algorithm.
     * @param algo The algorithm to configure.
     * @return The command (for chaining).
     */
    public TrainModelTask setAlgorithm(LenskitAlgorithmInstance algo) {
        algorithm = algo;
        return this;
    }

    /**
     * Specify a file to write. The trained recommender algorithm will be written
     * to this file.
     * @param file The file name.
     * @return The command (for chaining).
     */
    public TrainModelTask setWriteFile(File file) {
        writeFile = file;
        return this;
    }

    /**
     * Specify the data source to train on.
     * @param data The input data source.
     * @return The builder (for chaining).
     */
    public TrainModelTask setInput(DataSource data) {
        inputData = data;
        return this;
    }

    /**
     * Set the action to invoke.  The action's return value will be returned
     * from {@link #perform()}.
     * @param act The action to invoke.
     * @return The command (for chaining).
     */
    public TrainModelTask setAction(Function<LenskitRecommender,T> act) {
        action = act;
        return this;
    }

    @Override
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public T perform() throws TaskExecutionException {
        Preconditions.checkState(algorithm != null, "no algorithm specified");
        Preconditions.checkState(inputData != null, "no input data specified");
        Preconditions.checkState(inputData != null, "no action specified");
        LogContext context = new LogContext();
        try {
            context.put("lenskit.eval.command.class", getName());
            context.put("lenskit.eval.command.name", getName());
            context.put("lenskit.eval.algorithm.name", algorithm.getName());
            Closer closer = Closer.create();
            try {
                DAOFactory daoFactory = inputData.getDAOFactory();
                DataAccessObject dao = closer.register(daoFactory.snapshot());
                // TODO Support serializing the recommender
                LenskitRecommender rec;
                StopWatch timer = new StopWatch();
                timer.start();
                try {
                    logger.info("{}: building recommender {}", getName(), algorithm.getName());
                    rec = closer.register(algorithm.buildRecommender(
                            dao, null, inputData.getPreferenceDomain(), null, false));
                } catch (RecommenderBuildException e) {
                    throw new TaskExecutionException(getName() + ": error building recommender", e);
                }
                timer.stop();
                logger.info("{}: trained in {}", getName(), timer);
                return action.apply(rec);
            } catch (Throwable th) {
                throw closer.rethrow(th, TaskExecutionException.class);
            } finally {
                closer.close();
            }
        } catch (IOException ioe) {
            throw new TaskExecutionException("error in " + getName(), ioe);
        } finally {
            context.finish();
        }
    }
}

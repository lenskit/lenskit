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
package org.grouplens.lenskit.eval.traintest;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.snapshot.PackedPreferenceSnapshot;
import org.grouplens.lenskit.eval.Job;
import org.grouplens.lenskit.eval.JobGroup;
import org.grouplens.lenskit.eval.SharedPreferenceSnapshot;
import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.TestUserMetric;
import org.grouplens.lenskit.util.SoftLazyValue;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Run train-test evaluations of several algorithms over a data set.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public class TrainTestEvalJobGroup implements JobGroup {
    private static final Logger logger = LoggerFactory.getLogger(TrainTestEvalJobGroup.class);

    private TTDataSet dataSet;
    private List<Job> jobs;

    private TrainTestEvalCommand evaluation;


    public TrainTestEvalJobGroup(TrainTestEvalCommand eval,
                                 List<AlgorithmInstance> algos,
                                 List<TestUserMetric> evals,
                                 List<ModelMetric> modelMetrics,
                                 TTDataSet data, int partition,
                                 int numRecs) {
        evaluation = eval;
        dataSet = data;

        final Supplier<SharedPreferenceSnapshot> snap =
                new SoftLazyValue<SharedPreferenceSnapshot>(new Callable<SharedPreferenceSnapshot>() {
                    @Override
                    public SharedPreferenceSnapshot call() {
                        logger.info("Loading snapshot for {}", getName());
                        StopWatch timer = new StopWatch();
                        timer.start();
                        SharedPreferenceSnapshot snap = loadSnapshot();
                        timer.stop();
                        logger.info("Rating snapshot for {} loaded in {}",
                                    getName(), timer);
                        return snap;
                    }
                });

        jobs = new ArrayList<Job>(algos.size());
        for (AlgorithmInstance algo : algos) {
            Function<TableWriter, TableWriter> prefix = eval.prefixFunction(algo, data);
            TrainTestEvalJob job = new TrainTestEvalJob(
                    algo, evals, modelMetrics, eval.getPredictionChannels(), data, snap,
                    Suppliers.compose(prefix, evaluation.outputTableSupplier()),
                    Suppliers.compose(prefix, evaluation.userTableSupplier()),
                    Suppliers.compose(prefix, evaluation.predictTableSupplier()),
                    numRecs);
            jobs.add(job);
        }
    }

    @Override
    public String getName() {
        return dataSet.getName();
    }

    @Override
    public void start() {
        /* nothing to do */
    }

    @Override
    public void finish() {
        dataSet.release();
    }

    @Override
    public List<Job> getJobs() {
        return jobs;
    }

    private SharedPreferenceSnapshot loadSnapshot() {
        DataAccessObject dao = dataSet.getTrainFactory().create();
        try {
            return new SharedPreferenceSnapshot(new PackedPreferenceSnapshot.Provider(dao).get());
        } finally {
            dao.close();
        }
    }
}

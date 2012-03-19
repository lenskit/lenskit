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
package org.grouplens.lenskit.eval.traintest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.time.StopWatch;
import org.grouplens.lenskit.data.dao.DataAccessObject;
import org.grouplens.lenskit.data.snapshot.PackedRatingSnapshot;
import org.grouplens.lenskit.eval.AlgorithmInstance;
import org.grouplens.lenskit.eval.Job;
import org.grouplens.lenskit.eval.JobGroup;
import org.grouplens.lenskit.eval.PreparationContext;
import org.grouplens.lenskit.eval.PreparationException;
import org.grouplens.lenskit.eval.SharedRatingSnapshot;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.metrics.EvalMetric;
import org.grouplens.lenskit.util.LazyValue;
import org.grouplens.lenskit.util.tablewriter.TableWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Run train-test evaluations of several algorithms over a data set.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TTPredictEvalJobGroup implements JobGroup {
    private static final Logger logger = LoggerFactory.getLogger(TTPredictEvalJobGroup.class);
    
    private TTDataSet dataSet;
    private List<Job> jobs;

    private TTPredictEvaluation evaluation;

    public TTPredictEvalJobGroup(TTPredictEvaluation eval,
                                 List<AlgorithmInstance> algos,
                                 List<EvalMetric> evals,
                                 TTDataSet data) {
        evaluation = eval;
        dataSet = data;

        final Supplier<SharedRatingSnapshot> snap =
                new LazyValue<SharedRatingSnapshot>(new Callable<SharedRatingSnapshot>() {
                    @Override
                    public SharedRatingSnapshot call() {
                        logger.info("Loading snapshot for {}", getName());
                        StopWatch timer = new StopWatch();
                        timer.start();
                        SharedRatingSnapshot snap = loadSnapshot();
                        timer.stop();
                        logger.info("Rating snapshot for {} loaded in {}",
                                    getName(), timer);
                        return snap;
                    }
                });
        
        jobs = new ArrayList<Job>(algos.size());
        for (AlgorithmInstance algo: algos) {
            Function<TableWriter, TableWriter> prefix = eval.prefixFunction(algo, data);
            TTPredictEvalJob job = new TTPredictEvalJob(
                    algo, evals, data, snap,
                    Suppliers.compose(prefix, evaluation.outputTableSupplier()));
            job.setUserOutput(Suppliers.compose(prefix, evaluation.userTableSupplier()));
            job.setPredictOutput(Suppliers.compose(prefix, evaluation.predictTableSupplier()));
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
    
    private SharedRatingSnapshot loadSnapshot() {
        DataAccessObject dao = dataSet.getTrainFactory().create();
        try {
            return new SharedRatingSnapshot(new PackedRatingSnapshot.Builder(dao).build());
        } finally {
            dao.close();
        }
    }

    public long lastUpdated() {
        return dataSet.lastUpdated();
    }
}

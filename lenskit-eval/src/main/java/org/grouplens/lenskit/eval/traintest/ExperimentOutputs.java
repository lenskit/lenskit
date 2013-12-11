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

import org.grouplens.lenskit.eval.algorithm.AlgorithmInstance;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.table.writer.TableWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The outputs for an experiment.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ExperimentOutputs {
    private final ExperimentOutputLayout layouts;
    @Nonnull
    private final TableWriter resultsWriter;
    @Nullable
    private final TableWriter userWriter;
    @Nullable
    private final TableWriter predictionWriter;
    @Nullable
    private final TableWriter recommendationWriter;

    public ExperimentOutputs(ExperimentOutputLayout eol,
                             @Nonnull TableWriter results,
                             @Nullable TableWriter user,
                             @Nullable TableWriter predict,
                             @Nullable TableWriter rec) {
        layouts = eol;
        resultsWriter = results;
        userWriter = user;
        predictionWriter = predict;
        recommendationWriter = rec;
    }

    @Nonnull
    public TableWriter getResultsWriter() {
        return resultsWriter;
    }

    @Nullable
    public TableWriter getUserWriter() {
        return userWriter;
    }

    @Nullable
    public TableWriter getPredictionWriter() {
        return predictionWriter;
    }

    @Nullable
    public TableWriter getRecommendationWriter() {
        return recommendationWriter;
    }

    public ExperimentOutputs getPrefixed(AlgorithmInstance algo, TTDataSet data) {
        TableWriter results = layouts.prefixTable(resultsWriter, algo, data);
        TableWriter user = layouts.prefixTable(userWriter, algo, data);
        TableWriter predict = layouts.prefixTable(predictionWriter, algo, data);
        TableWriter recommend = layouts.prefixTable(recommendationWriter, algo, data);
        return new ExperimentOutputs(layouts, results, user, predict, recommend);
    }
}

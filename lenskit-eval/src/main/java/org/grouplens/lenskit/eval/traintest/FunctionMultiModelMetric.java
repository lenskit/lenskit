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
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.Recommender;
import org.grouplens.lenskit.eval.Attributed;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.CSVWriter;
import org.grouplens.lenskit.util.table.writer.TableWriter;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Model metric backed by an arbitrary function that returns multiple rows per algorithmInfo.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class FunctionMultiModelMetric implements ModelMetric {
    private final File outputFile;
    private final List<String> columnHeaders;
    private final Function<Recommender, List<List<Object>>> function;
    private TableWriter writer;
    private ExperimentOutputLayout evalLayout;

    public FunctionMultiModelMetric(File file, List<String> columns,
                                    Function<Recommender, List<List<Object>>> func) {
        outputFile = file;
        columnHeaders = Lists.newArrayList(columns);
        function = func;
    }

    @Override
    public List<String> getColumnLabels() {
        return Collections.emptyList();
    }

    @Override
    public List<Object> measureAlgorithm(Attributed algorithm, TTDataSet data, Recommender recommender) {
        Preconditions.checkState(evalLayout != null, "evaluation not in progress");
        TableWriter w = evalLayout.prefixTable(writer, algorithm, data);
        for (List<Object> row: function.apply(recommender)) {
            try {
                w.writeRow(row.toArray());
            } catch (IOException e) {
                throw new RuntimeException("error writing row", e);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void startEvaluation(TrainTestEvalTask eval) {
        evalLayout = eval.getOutputLayout();

        TableLayoutBuilder builder = TableLayoutBuilder.copy(eval.getOutputLayout().getCommonLayout());
        for (String col: columnHeaders) {
            builder.addColumn(col);
        }
        try {
            writer = CSVWriter.open(outputFile, builder.build());
        } catch (IOException e) {
            throw new RuntimeException("error opening output file", e);
        }
    }

    @Override
    public void finishEvaluation() {
        evalLayout = null;
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("error closing output file", e);
        }
    }
}

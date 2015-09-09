/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.eval.traintest;

import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;
import org.grouplens.lenskit.util.table.writer.TableWriter;
import org.grouplens.lenskit.util.table.writer.TableWriters;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Manages the layouts for experiment output tables.  Handles the common columns describing algorithms and data sets,
 * and makes it easier to define new output tables that augment these columns with additional data.
 */
public class ExperimentOutputLayout {
    private final Map<String, Integer> dataColumns;
    private final Map<String, Integer> algoColumns;
    private TableLayout conditionLayout;

    public ExperimentOutputLayout(Set<String> dataCols, Set<String> algoCols) {
        algoColumns = new HashMap<>();
        dataColumns = new HashMap<>();
        TableLayoutBuilder tlb = new TableLayoutBuilder();
        for (String dc: dataCols) {
            int i = tlb.getColumnCount();
            dataColumns.put(dc, i);
            tlb.addColumn(dc);
        }
        for (String ac: algoCols) {
            int i = tlb.getColumnCount();
            algoColumns.put(ac, i);
            tlb.addColumn(ac);
        }

        conditionLayout = tlb.build();
    }

    public TableLayout getConditionLayout() {
        return conditionLayout;
    }

    public int getConditionColumnCount() {
        return conditionLayout.getColumnCount();
    }

    public int getDataColumn(String name) {
        Integer idx = dataColumns.get(name);
        if (idx == null) {
            throw new IllegalArgumentException("no such data column " + name);
        } else {
            return idx;
        }
    }

    public int getAlgorithmColumn(String name) {
        Integer idx = algoColumns.get(name);
        if (idx == null) {
            throw new IllegalArgumentException("no such algorithm column " + name);
        } else {
            return idx;
        }
    }

    /**
     * Prefix a table for a particular algorithm and data set.
     *
     * @param base      The table to prefix.
     * @param data      The data set to prefix for.
     * @param algorithm The algorithm to prefix for.
     * @return A prefixed table, suitable for outputting the results of evaluating
     * {@code algorithmInfo} on {@code data}, or {@code null} if {@code base} is null.
     */
    public TableWriter prefixTable(@Nullable TableWriter base,
                                   @Nonnull DataSet data,
                                   @Nonnull AlgorithmInstance algorithm) {
        if (base == null) {
            return null;
        }

        Object[] prefix = new Object[getConditionColumnCount()];
        for (Map.Entry<String, Object> attr : data.getAttributes().entrySet()) {
            int idx = getDataColumn(attr.getKey());
            prefix[idx] = attr.getValue();
        }

        for (Map.Entry<String, Object> attr : algorithm.getAttributes().entrySet()) {
            int idx = getAlgorithmColumn(attr.getKey());
            prefix[idx] = attr.getValue();
        }

        return TableWriters.prefixed(base, prefix);
    }
}

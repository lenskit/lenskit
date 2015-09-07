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

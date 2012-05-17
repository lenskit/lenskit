package org.grouplens.lenskit.util.tablewriter;

import org.grouplens.lenskit.eval.util.table.ResultTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of {@link TableWriter} for in memory result table
 *
 * @author Shuo Chang<schang@cs.umn.edu>.
 */
public class InMemoryWriter implements TableWriter {
    private ResultTable result;
    private TableLayout layout;
    private Object[] buffer;

    /**
     * Construct a new in memory writer.
     * @param r The underlying result to output to.
     * @param l The table layout, or {@code null} if the table has no headers.
     */
    public InMemoryWriter(@Nonnull ResultTable r, @Nullable TableLayout l) {
        layout = l;
        result = r;
        if(layout != null) {
            result.setHeader(layout.getColumnHeaders());
        }
    }

    @Override
    public void close() {}


    @Override
    public synchronized void writeRow(Object[] row) {
        if (layout != null && row.length > layout.getColumnCount()) {
            throw new IllegalArgumentException("row too long");
        }
        result.addResultRow(row);
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    public ResultTable getResult() {
        return result;
    }
}

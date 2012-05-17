package org.grouplens.lenskit.util.tablewriter;

import org.grouplens.lenskit.eval.util.table.TableImpl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Implementation of {@link TableWriter} for in memory result table
 *
 * @author Shuo Chang<schang@cs.umn.edu>.
 */
public class InMemoryWriter implements TableWriter {
    private TableImpl result;
    private TableLayout layout;
    private Object[] buffer;

    /**
     * Construct a new in memory writer.
     * @param l The table layout, or {@code null} if the table has no headers.
     */
    public InMemoryWriter(@Nullable TableLayout l) {
        layout = l;
        result = new TableImpl(layout.getColumnHeaders());
    }

    @Override
    public void close() {}


    @Override
    public synchronized void writeRow(Object[] row) {
        result.addResultRow(row);
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    public TableImpl getResult() {
        return result;
    }
}

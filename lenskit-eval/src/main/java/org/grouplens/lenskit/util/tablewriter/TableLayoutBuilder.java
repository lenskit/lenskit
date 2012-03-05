package org.grouplens.lenskit.util.tablewriter;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.exception.CloneFailedException;

import java.util.ArrayList;

/**
 * Construct a layout for a table.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class TableLayoutBuilder implements Builder<TableLayout>, Cloneable {
    private ArrayList<String> columns = new ArrayList<String>();

    /**
     * Add a column to the table layout.
     * @param header The column header.
     * @return The index of the column. Columns are indexed from 0.
     */
    public int addColumn(String header) {
        int i = columns.size();
        columns.add(header);
        return i;
    }

    /**
     * Get the number of columns currently in the layout.
     * @return The number of columns in the layout.
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Clone this layout builder. Used to build multiple layouts from the same initial
     * columns.
     * @return An independent copy of this table layout builder.
     */
    @Override
    public TableLayoutBuilder clone() {
        TableLayoutBuilder copy = null;
        try {
            copy = (TableLayoutBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CloneFailedException(e);
        }
        copy.columns = new ArrayList<String>(columns);
        return copy;
    }

    @Override
    public TableLayout build() {
        return new TableLayout(columns);
    }
}

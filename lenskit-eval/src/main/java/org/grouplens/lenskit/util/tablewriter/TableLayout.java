package org.grouplens.lenskit.util.tablewriter;

import java.util.Collections;
import java.util.List;

/**
 * A layout for a table to be written. Specifies the columns in the table.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class TableLayout {
    private List<String> columnHeaders;

    TableLayout(List<String> headers) {
        columnHeaders = headers;
    }

    /**
     * Get the headers of the columns.
     * @return The headers of the columns in the table layout.
     */
    public List<String> getColumnHeaders() {
        return Collections.unmodifiableList(columnHeaders);
    }

    /**
     * Get the number of columns in this layout.
     * @return The number of columns in the table layout.
     */
    public int getColumnCount() {
        return columnHeaders.size();
    }
}

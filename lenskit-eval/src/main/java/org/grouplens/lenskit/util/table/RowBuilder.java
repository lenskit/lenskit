package org.grouplens.lenskit.util.table;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Build a row.  This builds a row from named columns; to use column indexes, just build a list.
 */
public class RowBuilder {
    private final TableLayout layout;
    private List<Object> values;

    public RowBuilder(TableLayout tl) {
        layout = tl;
        values = new ArrayList<>(layout.getColumnCount());
    }

    /**
     * Add a single column by name.
     * @param name The column name.
     * @param value The column value.
     * @return The row builder (for chaining).
     */
    public RowBuilder add(String name, Object value) {
        int idx = layout.columnIndex(name);
        while (values.size() <= idx) {
            values.add(null);
        }
        values.set(idx, value);
        return this;
    }

    /**
     * Add several columns from a map.
     * @param columns The columns.
     * @return The row builder (for chaining).
     */
    public RowBuilder addAll(Map<String,?> columns) {
        for (Map.Entry<String,?> e: columns.entrySet()) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    /**
     * Clear the row builder.
     * @return The row builder (for chaining).
     */
    public RowBuilder clear() {
        values.clear();
        return this;
    }

    /**
     * Build a row as a row object.
     * @return The row.
     */
    public Row build() {
        return new RowImpl(layout, values.toArray());
    }

    /**
     * Build the row as a list.
     * @return The list of fields.
     */
    public List<Object> buildList() {
        return ImmutableList.copyOf(values);
    }
}

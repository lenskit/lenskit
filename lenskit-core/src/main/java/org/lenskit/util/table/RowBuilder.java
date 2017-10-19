/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.table;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Build a row.  This builds a row from named columns; to use column indexes, just build a list.
 */
public class RowBuilder {
    private final TableLayout layout;
    private Object[] values;

    public RowBuilder(TableLayout tl) {
        layout = tl;
        values = new Object[layout.getColumnCount()];
    }

    /**
     * Add a single column by name.
     * @param name The column name.
     * @param value The column value.
     * @return The row builder (for chaining).
     */
    public RowBuilder add(String name, Object value) {
        int idx = layout.columnIndex(name);
        values[idx] = value;
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
        Arrays.fill(values, null);
        return this;
    }

    /**
     * Build a row as a row object.
     * @return The row.
     */
    public Row build() {
        return new RowImpl(layout, values);
    }

    /**
     * Build the row as a list.
     * @return The list of fields.
     */
    public List<Object> buildList() {
        return new ObjectArrayList<>(values);
    }
}

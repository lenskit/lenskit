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
package org.grouplens.lenskit.util.table;

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

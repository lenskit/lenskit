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

import org.apache.commons.lang3.builder.Builder;

import java.util.Arrays;
import java.util.LinkedHashSet;

/**
 * Construct a layout for a table.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class TableLayoutBuilder implements Builder<TableLayout> {
    private LinkedHashSet<String> columns = new LinkedHashSet<>();

    /**
     * Construct a new builder that is a copy of an existing layout.
     *
     * @param layout The layout to copy.
     * @return A new builder containing all columns in the layout.
     */
    public static TableLayoutBuilder copy(TableLayout layout) {
        TableLayoutBuilder bld = new TableLayoutBuilder();
        for (String col: layout.getColumns()) {
            bld.addColumn(col);
        }
        return bld;
    }

    /**
     * Add a column to the table layout.  Each column must have a unique, non-null
     * name.
     *
     * @param name The column name.
     * @return The builder, for chaining.
     * @throws IllegalArgumentException if the column already exists.
     */
    public TableLayoutBuilder addColumn(String name) {
        if (name == null) {
            throw new NullPointerException("column name");
        }
        if (columns.contains(name)) {
            throw new IllegalArgumentException("column " + name + " already exists");
        }
        columns.add(name);
        return this;
    }

    /**
     * Add columns to the layout.
     * @param columns The columns to add.
     * @return The layout builder (for chaining).
     */
    public TableLayoutBuilder addColumns(Iterable<String> columns) {
        for (String c: columns) {
            addColumn(c);
        }
        return this;
    }

    /**
     * Add columns to the layout.
     * @param columns The columns to add.
     * @return The layout builder (for chaining).
     */
    public TableLayoutBuilder addColumns(String... columns) {
        return addColumns(Arrays.asList(columns));
    }

    /**
     * Get the number of columns currently in the layout.
     *
     * @return The number of columns in the layout.
     */
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public TableLayout build() {
        return new TableLayout(columns);
    }
}

/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.util.tablewriter;

import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.lang3.exception.CloneFailedException;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Construct a layout for a table.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
public class TableLayoutBuilder implements Builder<TableLayout>, Cloneable {
    private LinkedHashSet<String> columns = new LinkedHashSet<String>();

    /**
     * Add a column to the table layout.  Each column must have a unique, non-null
     * name.
     *
     * @param name The column name.
     * @return The index of the column. Columns are indexed from 0.
     * @throws IllegalArgumentException if the column already exists.
     */
    public int addColumn(String name) {
        if (name == null) {
            throw new IllegalArgumentException("column name cannot be null");
        }
        if (columns.contains(name)) {
            throw new IllegalArgumentException("column " + name + " already exists");
        }
        int i = columns.size();
        columns.add(name);
        return i;
    }

    /**
     * Get the number of columns currently in the layout.
     *
     * @return The number of columns in the layout.
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Clone this layout command. Used to build multiple layouts from the same initial
     * columns.
     *
     * @return An independent copy of this table layout command.
     */
    @Override
    public TableLayoutBuilder clone() {
        TableLayoutBuilder copy = null;
        try {
            copy = (TableLayoutBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new CloneFailedException(e);
        }
        copy.columns = new LinkedHashSet<String>(columns);
        return copy;
    }

    @Override
    public TableLayout build() {
        return new TableLayout(columns);
    }
}

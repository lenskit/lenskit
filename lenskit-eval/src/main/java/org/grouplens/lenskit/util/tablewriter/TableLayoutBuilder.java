/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

/**
 * Construct a layout for a table.
 *
 * @author Michael Ekstrand
 * @since 0.10
 */
public class TableLayoutBuilder implements Builder<TableLayout>, Cloneable {
    private ArrayList<String> columns = new ArrayList<String>();

    /**
     * Add a column to the table layout.
     *
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
        copy.columns = new ArrayList<String>(columns);
        return copy;
    }

    @Override
    public TableLayout build() {
        return new TableLayout(columns);
    }
}

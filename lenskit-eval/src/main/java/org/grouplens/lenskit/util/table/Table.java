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
package org.grouplens.lenskit.util.table;

import org.grouplens.lenskit.util.tablewriter.TableLayout;

import java.util.List;

/**
 * This is the interface for the in memory table which stores a list of rows. Users should be able
 * to call the filter method to find the rows that satisfy the conditions specified by users. And
 * table expose the functions of columns to enable users calling the functions on column.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Table extends List<Row> {
    Table filter(String header, Object data);

    /**
     * Get a column by index.
     * @param idx The column index (starting from 0).
     * @return The column.
     * @throws IllegalArgumentException if {@var idx} is out of bounds.
     */
    Column column(int idx);

    /**
     * Get a column by name.
     * @param col The column name.
     * @return The column.
     * @throws IllegalArgumentException if {@var col} is not a valid column.
     */
    Column column(String col);

    /**
     * Get the layout of this table.
     * @return The table layout.
     */
    TableLayout getLayout();

    @Deprecated
    List<String> getHeader();
}

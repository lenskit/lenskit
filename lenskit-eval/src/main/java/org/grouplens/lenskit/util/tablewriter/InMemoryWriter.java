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

import org.grouplens.lenskit.util.table.TableImpl;

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
     *
     * @param l The table layout, or {@code null} if the table has no headers.
     */
    public InMemoryWriter(@Nullable TableLayout l) {
        layout = l;
        result = new TableImpl(layout.getColumnHeaders());
    }

    @Override
    public void close() {
    }


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

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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.util.table.Table;
import org.grouplens.lenskit.util.table.TableImpl;

import javax.annotation.Nullable;

/**
 * Implementation of {@link TableWriter} for in memory result table
 *
 * @author Shuo Chang<schang@cs.umn.edu>.
 */
public class InMemoryWriter implements TableWriter, Builder<Table> {
    private TableImpl result;
    private TableLayout layout;
    private Object[] buffer;

    /**
     * Construct a new in memory writer.
     *
     * @param l The table layout.
     */
    public InMemoryWriter(TableLayout l) {
        if (l == null) {
            throw new IllegalArgumentException("table layout cannot be null");
        }
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

    @Override
    public Table build() {
        return result;
    }
}

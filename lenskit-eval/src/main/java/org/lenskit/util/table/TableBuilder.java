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
package org.lenskit.util.table;

import org.apache.commons.lang3.builder.Builder;
import org.lenskit.util.table.writer.AbstractTableWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to construct tables.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TableBuilder extends AbstractTableWriter implements Builder<Table> {
    private final TableLayout layout;
    private final List<Row> rows;

    /**
     * Construct a new builder using a particular layout.
     *
     * @param layout The table layout.
     */
    public TableBuilder(TableLayout layout) {
        this.layout = layout;
        rows = new ArrayList<Row>();
    }

    public TableBuilder(List<String> columns) {
        TableLayoutBuilder bld = new TableLayoutBuilder();
        for (String col: columns) {
            bld.addColumn(col);
        }
        layout = bld.build();

        rows = new ArrayList<Row>();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public void close() {}


    @Override
    public void writeRow(List<?> row) {
        addRow(row);
    }

    /**
     * Add a row to the table.
     *
     * @param row The row to add.
     * @throws IllegalArgumentException if the row has the wrong length.
     * @since 1.1
     */
    public synchronized void addRow(List<?> row) {
        addRow(row.toArray());
    }

    /**
     * Add a row to the table.
     *
     * @param row The row to add.
     * @throws IllegalArgumentException if the row has the wrong length.
     * @since 1.1
     */
    public synchronized void addRow(Object... row) {
        checkRowWidth(row.length);
        rows.add(new RowImpl(layout, row));
    }

    @Override
    public Table build() {
        return new TableImpl(layout, rows);
    }
}

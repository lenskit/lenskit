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

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.util.tablewriter.TableLayout;
import org.grouplens.lenskit.util.tablewriter.TableLayoutBuilder;
import org.grouplens.lenskit.util.tablewriter.TableWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to construct tables.
 *
 * @author Shuo Chang
 * @author Michael Ekstrand
 */
public class TableBuilder implements Builder<Table>, TableWriter {
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
    public void writeRow(Object[] row) {
        addRow(row);
    }

    /**
     * Add a row to the table.
     *
     * @param row The row to add.
     */
    public synchronized void addRow(Object[] row) {
        rows.add(new RowImpl(layout, row));
    }

    @Override
    public Table build() {
        return new TableImpl(layout, rows);
    }
}

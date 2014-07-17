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
package org.grouplens.lenskit.util.table.writer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.grouplens.lenskit.util.table.TableLayout;
import org.grouplens.lenskit.util.table.TableLayoutBuilder;

import java.io.IOException;
import java.util.List;

/**
 * A table writer that has the initial column values fixed.  It wraps a table
 * writer and presents a writer with fewer columns and constant values put in
 * for the underlying missing columns.  Closing it does nothing.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class PrefixedTableWriter extends AbstractTableWriter {
    private ObjectArrayList<Object> rowData;
    private int fixedColumns;
    private TableLayout layout;
    private TableWriter baseWriter;

    /**
     * Construct a new prefixed table writer.
     *
     * @param writer The underlying table writer to write to.  This writer is <b>not</b> closed
     *               when the prefixed writer is closed.
     * @param values The initial values to write. Each row written to this table
     *               writer is written to the base writer with these values prefixed.
     */
    public PrefixedTableWriter(TableWriter writer, List<?> values) {
        baseWriter = writer;
        TableLayout baseLayout = writer.getLayout();
        if (values.size() > baseLayout.getColumnCount()) {
            throw new IllegalArgumentException("Value array too wide");
        }

        rowData = new ObjectArrayList<Object>(writer.getLayout().getColumnCount());
        rowData.addAll(values);

        fixedColumns = values.size();

        TableLayoutBuilder bld = new TableLayoutBuilder();
        List<String> bheaders = baseLayout.getColumns();
        for (String h : bheaders.subList(fixedColumns, bheaders.size())) {
            bld.addColumn(h);
        }
        layout = bld.build();
        assert layout.getColumnCount() + rowData.size() == writer.getLayout().getColumnCount();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public synchronized void writeRow(List<?> row) throws IOException {
        checkRowWidth(row.size());

        // Add the data to the row
        assert rowData.size() == fixedColumns;
        rowData.addAll(row);

        try {
            // write the row
            baseWriter.writeRow(rowData);
        } finally {
            // reset the row
            rowData.removeElements(fixedColumns, rowData.size());
        }
    }
}

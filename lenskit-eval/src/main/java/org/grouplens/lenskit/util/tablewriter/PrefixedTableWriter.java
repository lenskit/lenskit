/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A table writer that has the initial column values fixed.  It wraps a table
 * writer and presents a writer with fewer columns and constant values put in
 * for the underlying missing columns.  Closing it does nothing.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
class PrefixedTableWriter implements TableWriter {
    private String[] rowData; // contains fixed values; other columns re-used
    private int fixedColumns;
    private TableLayout layout;
    private TableWriter baseWriter;
    
    /**
     * Construct a new prefixed table writer.
     * 
     * @param writer The underlying table writer to write to.
     * @param values The initial values to write. Each row written to this table
     *        writer is written to the base writer with these values prefixed.
     */
    public PrefixedTableWriter(TableWriter writer, String[] values) {
        baseWriter = writer;
        TableLayout baseLayout = writer.getLayout();
        if (values.length > baseLayout.getColumnCount()) {
            throw new IllegalArgumentException("Value array too wide");
        }
        
        rowData = Arrays.copyOf(values, baseLayout.getColumnCount());
        fixedColumns = values.length;

        TableLayoutBuilder bld = new TableLayoutBuilder();
        List<String> bheaders = baseLayout.getColumnHeaders();
        for (String h: bheaders.subList(fixedColumns, bheaders.size())) {
            bld.addColumn(h);
        }
        layout = bld.build();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public synchronized void writeRow(String[] row) throws IOException {
        if (row.length > layout.getColumnCount()) {
            throw new IllegalArgumentException("Row too wide");
        }
        
        // blit row data to end of re-used array
        System.arraycopy(row, 0, rowData, fixedColumns, row.length);
        // fill remaining elements with null
        Arrays.fill(rowData, fixedColumns + row.length, rowData.length, null);
        // and write the row
        baseWriter.writeRow(rowData);
    }

    @Override
    public void close() throws IOException {
        /* no-op */
    }
}

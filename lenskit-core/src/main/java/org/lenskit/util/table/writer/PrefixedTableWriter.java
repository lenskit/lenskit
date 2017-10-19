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
package org.lenskit.util.table.writer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.lenskit.util.table.TableLayout;
import org.lenskit.util.table.TableLayoutBuilder;

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

        rowData = new ObjectArrayList<>(writer.getLayout().getColumnCount());
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

    @Override
    public void flush() throws IOException {
        baseWriter.flush();
    }

    /**
     * No-op close implementation. Closing a prefixed writer does *not* close the
     * underlying writer.  It does, however, flush it.
     * @throws IOException if there is an error flushing the writer.
     */
    @Override
    public void close() throws IOException {
        baseWriter.flush();
    }
}

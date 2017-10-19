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
package org.lenskit.util.table;

import org.apache.commons.lang3.builder.Builder;
import org.lenskit.util.table.writer.AbstractTableWriter;

import java.io.IOException;
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
        rows = new ArrayList<>();
    }

    public TableBuilder(List<String> columns) {
        TableLayoutBuilder bld = new TableLayoutBuilder();
        for (String col: columns) {
            bld.addColumn(col);
        }
        layout = bld.build();

        rows = new ArrayList<>();
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    @Override
    public void close() {}

    @Override
    public void flush() throws IOException {}

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

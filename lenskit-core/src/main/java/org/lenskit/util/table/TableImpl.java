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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import net.jcip.annotations.Immutable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * In-memory table implementation.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Immutable
class TableImpl extends AbstractList<Row> implements Table {
    private ArrayList<Row> rows;

    private final TableLayout layout;

    /**
     * Construct a new table.  This constructor should only be called from
     * {@link TableBuilder}.
     *
     * @param layout The table layout.
     * @param rws The table rows. Each row must have the same layout.
     */
    TableImpl(TableLayout layout, Iterable<Row> rws) {
        super();
        rows = Lists.newArrayList(rws);
        this.layout = layout;
    }

    /**
     * Filter the table with the given matching data.
     *
     * @param col  The name of the column
     * @param data The data in the column to match
     * @return A new table that has "data" in "col"
     */
    @Override
    public TableImpl filter(final String col, final Object data) {
        Predicate<Row> pred = new Predicate<Row>() {
            @Override
            public boolean apply(Row input) {
                return data.equals(input.value(col));
            }
        };
        Iterable<Row> filtered = Iterables.filter(this.rows, pred);
        return new TableImpl(layout, filtered);
    }

    @Override
    public int size() {
        return rows.size();
    }

    @Override @Nonnull
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    @Override
    public Row get(int i) {
        return rows.get(i);
    }

    @Override
    public ColumnImpl column(String col) {
        return new ColumnImpl(this, layout.columnIndex(col), col);
    }

    @Override
    public ColumnImpl column(int idx) {
        Preconditions.checkElementIndex(idx, layout.getColumnCount(), "column");
        return new ColumnImpl(this, idx, layout.getColumns().get(idx));
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }
}

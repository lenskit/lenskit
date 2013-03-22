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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * In-memory table implementation.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
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

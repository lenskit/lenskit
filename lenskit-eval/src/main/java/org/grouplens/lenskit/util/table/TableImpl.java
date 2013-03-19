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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.grouplens.lenskit.util.tablewriter.TableLayout;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The implementation of the im memory table, which is the replica of the csv table output to the
 * file.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
@Immutable
class TableImpl extends AbstractList<Row> implements Table {
    private ArrayList<Row> rows;

    private final TableLayout layout;

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
        return new ColumnImpl(layout.columnIndex(col));
    }

    @Override
    public ColumnImpl column(int idx) {
        if (idx < 0 || idx >= layout.getColumnCount()) {
            String msg = String.format("column index %d not in range [%d,%d)",
                                       idx, 0, layout.getColumnCount());
            throw new IllegalArgumentException(msg);
        }
        return new ColumnImpl(idx);
    }

    @Override
    public TableLayout getLayout() {
        return layout;
    }

    public class ColumnImpl extends AbstractList<Object> implements Column {
        ArrayList<Object> column;

        ColumnImpl(int col) {
            super();
            column = new ArrayList<Object>();
            for (Row row : rows) {
                column.add(row.value(col));
            }
        }

        @Override
        public double sum() {
            double sum = 0;
            if (column.size() == 0 ||
                    !Number.class.isAssignableFrom(column.get(0).getClass())) {
                return Double.NaN;
            } else {
                for (Object v : column) {
                    sum += ((Number) v).doubleValue();
                }
                return sum;
            }
        }

        @Override
        public double average() {
            if (column.size() == 0) {
                return Double.NaN;
            }
            return sum() / column.size();
        }

        @Override
        public int size() {
            return column.size();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object get(int i) {
            return column.get(i);  //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}

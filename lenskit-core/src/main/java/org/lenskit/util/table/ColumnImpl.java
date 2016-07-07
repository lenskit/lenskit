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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.Iterator;

/**
 * Implementation of a column.  This view is  facade on top of the table.
 */
class ColumnImpl extends AbstractList<Object> implements Column {
    private final Table table;
    private final int column;
    private final String columnName;

    /**
     * Construct a column implementation.
     * @param tbl The table.
     * @param col The column index.
     * @param name The column name.
     */
    public ColumnImpl(Table tbl, int col, String name) {
        table = tbl;
        column = col;
        columnName = name;
    }

    @Override
    public Object get(int index) {
        return table.get(index).value(column);
    }

    @Override
    public int size() {
        return table.size();
    }

    @Override @Nonnull
    public Iterator<Object> iterator() {
        return Iterators.transform(table.iterator(),
                                   new Function<Row, Object>() {
                                       @Nullable
                                       @Override
                                       public Object apply(@Nullable Row row) {
                                           assert row != null;
                                           return row.value(column);
                                       }
                                   });
    }

    @Override
    public double sum() {
        double sum = 0;
        for (Object v: this) {
            if (v instanceof Number) {
                sum += ((Number) v).doubleValue();
            } else if (v == null) {
                return Double.NaN;
            } else {
                throw new IllegalArgumentException(
                        String.format("non-numeric entry in column %d (%s)",
                                      column, columnName));
            }
        }
        return sum;
    }

    @Override
    public double average() {
        int sz = size();
        if (sz == 0) {
            return Double.NaN;
        } else {
            return sum() / size();
        }
    }
}

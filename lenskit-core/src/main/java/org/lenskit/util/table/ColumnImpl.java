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

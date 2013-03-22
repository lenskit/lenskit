package org.grouplens.lenskit.util.table;

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

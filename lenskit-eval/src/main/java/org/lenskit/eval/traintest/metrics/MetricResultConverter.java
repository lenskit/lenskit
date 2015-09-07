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
package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Utility class for converting typed results into row data for table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class MetricResultConverter<T> {
    private final Class<T> resultType;
    private final List<Column> columns;
    private final List<String> columnLabels;

    MetricResultConverter(Class<T> type, List<Column> cols) {
        resultType = type;
        columns = ImmutableList.copyOf(cols);
        columnLabels = Lists.transform(columns, new Function<Column, String>() {
            @Nullable
            @Override
            public String apply(@Nullable Column input) {
                assert input != null;
                return input.getName();
            }
        });
    }

    public Class<T> getResultType() {
        return resultType;
    }

    /**
     * Get the labels of the columns produced by this converter.
     * @return The column labels.
     */
    public List<String> getColumnLabels() {
        return columnLabels;
    }

    /**
     * Get the column values for a result instance.
     * @param result The result instance.
     * @return The list of column values.
     */
    public List<Object> getColumns(T result) {
        List<Object> row = Lists.newArrayList();
        for (Column col: columns) {
            if (result != null) {
                row.add(col.getValue(result));
            } else {
                row.add(null);
            }
        }
        return row;
    }

    public static <T> MetricResultConverter<T> create(Class<T> type) {
        List<Column> columns = Lists.newArrayList();
        for (Method m: type.getMethods()) {
            MetricColumn info = m.getAnnotation(MetricColumn.class);
            if (info != null) {
                columns.add(new MethodColumn(m));
            }
        }
        for (Field f: type.getFields()) {
            MetricColumn info = f.getAnnotation(MetricColumn.class);
            if (info != null) {
                columns.add(new FieldColumn(f));
            }
        }

        ImmutableList<Column> sorted =
                Ordering.natural()
                        .onResultOf(new Function<Column, Integer>() {
                            @Override
                            public Integer apply(@Nullable Column input) {
                                assert input != null;
                                int c = input.getInfo().order();
                                // negative values should sort last
                                return c >= 0 ? c : Integer.MAX_VALUE;
                            }
                        })
                        .immutableSortedCopy(columns);
        return new MetricResultConverter<>(type, sorted);
    }

    private abstract static class Column {
        private final MetricColumn info;

        Column(AnnotatedElement elem) {
            info = elem.getAnnotation(MetricColumn.class);
        }

        public String getName() {
            return info.value();
        }

        public MetricColumn getInfo() {
            return info;
        }

        public abstract Object getValue(Object obj);
    }

    private static class MethodColumn extends Column {
        private final Method method;

        MethodColumn(Method m) {
            super(m);
            method = m;
        }

        @Override
        public Object getValue(Object obj) {
            try {
                return method.invoke(obj);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            } catch (InvocationTargetException e) {
                throw Throwables.propagate(e.getCause());
            }
        }
    }

    private static class FieldColumn extends Column {
        private final Field field;

        FieldColumn(Field f) {
            super(f);
            field = f;
        }

        @Override
        public Object getValue(Object obj) {
            try {
                return field.get(obj);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}

package org.grouplens.lenskit.eval.metrics;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import javax.annotation.Nullable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for converting typed results into row data for table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
public class ResultConverter<T> {
    private final Class<T> resultType;
    private final List<Column> columns;
    private final List<String> columnLabels;

    ResultConverter(Class<T> type, List<Column> cols) {
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

    public static <T> ResultConverter<T> create(Class<T> type) {
        List<Column> columns = Lists.newArrayList();
        for (Method m: type.getMethods()) {
            ResultColumn info = m.getAnnotation(ResultColumn.class);
            if (info != null) {
                columns.add(new MethodColumn(m));
            }
        }
        for (Field f: type.getFields()) {
            ResultColumn info = f.getAnnotation(ResultColumn.class);
            if (info != null) {
                columns.add(new FieldColumn(f));
            }
        }
        Collections.sort(columns);

        return new ResultConverter<T>(type, columns);
    }

    private abstract static class Column implements Comparable<Column> {
        private final ResultColumn info;

        Column(AnnotatedElement elem) {
            info = elem.getAnnotation(ResultColumn.class);
        }

        public String getName() {
            return info.value();
        }

        @Override
        public int compareTo(Column o) {
            int col1 = info.order();
            if (col1 < 0) {
                col1 = Integer.MAX_VALUE;
            }
            int col2 = o.info.order();
            if (col2 < 0) {
                col2 = Integer.MAX_VALUE;
            }
            return Ints.compare(col1, col2);
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

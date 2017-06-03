/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Base class for metric results that expose their values via getters.
 */
public abstract class TypedMetricResult extends MetricResult {
    /**
     * Get the columns for a typed metric result class.
     * @param type The result class.
     * @return The column labels for results of type `type`.
     */
    public static List<String> getColumns(Class<? extends TypedMetricResult> type) {
        return getColumns(type, null);
    }

    /**
     * Get the columns for a typed metric result class.
     * @param type The result class.  If `null`, an empty list of columns will be returned.
     * @param suffix A suffix for column labels; if non-`null`, will be appended to each column label separated with
     *               a period.
     * @return The column labels for results of type `type`.
     */
    @Nonnull
    public static List<String> getColumns(@Nullable Class<? extends TypedMetricResult> type,
                                          @Nullable final String suffix) {
        if (type == null) {
            return Collections.emptyList();
        }
        List<ColumnDesc> columns = getColumnInfo(type);

        Ordering<ColumnDesc> order = Ordering.natural()
                                             .onResultOf(new Function<ColumnDesc, Integer>() {
                                                 @Override
                                                 public Integer apply(@Nullable ColumnDesc input) {
                                                     assert input != null;
                                                     int c = input.getAnnotation().order();
                                                     // negative values should sort last
                                                     return c >= 0 ? c : Integer.MAX_VALUE;
                                                 }
                                             });

        ImmutableList.Builder<String> names = ImmutableList.builder();
        for (ColumnDesc c: order.sortedCopy(columns)) {
            if (suffix == null) {
                names.add(c.getName());
            } else {
                names.add(c.getName() + "." + suffix);
            }
        }

        return names.build();
    }

    @Override
    public Map<String, Object> getValues() {
        Map<String,Object> values = new HashMap<>();

        for (ColumnDesc cd: getColumnInfo(getClass())) {
            values.put(cd.getName(), cd.getValue(this));
        }

        return values;
    }

    private static List<ColumnDesc> getColumnInfo(Class<?> cls) {
        Class<?> type = cls;
        List<ColumnDesc> columns = new ArrayList<>();
        while (type != null) {
            for (Field f: type.getDeclaredFields()) {
                MetricColumn info = f.getAnnotation(MetricColumn.class);
                if (info != null) {
                    columns.add(new FieldColumn(info, f));
                }
            }
            for (Method m: type.getDeclaredMethods()) {
                MetricColumn info = m.getAnnotation(MetricColumn.class);
                if (info != null) {
                    columns.add(new MethodColumn(info, m));
                }
            }
            type = type.getSuperclass();
        }
        return columns;
    }

    private abstract static class ColumnDesc {
        MetricColumn annot;

        public ColumnDesc(MetricColumn a) {
            annot = a;
        }

        public MetricColumn getAnnotation() {
            return annot;
        }

        public String getName() {
            return annot.value();
        }

        public abstract Object getValue(Object inst);
    }

    private static class FieldColumn extends ColumnDesc {
        private final Field field;

        public FieldColumn(MetricColumn a, Field f) {
            super(a);
            field = f;
        }

        @Override
        public Object getValue(Object inst) {
            field.setAccessible(true);
            try {
                return field.get(inst);
            } catch (IllegalAccessException e) {
                throw new VerifyException("cannot get " + field, e);
            }
        }
    }

    private static class MethodColumn extends ColumnDesc {
        private final Method method;

        public MethodColumn(MetricColumn a, Method m) {
            super(a);
            method = m;
        }

        @Override
        public Object getValue(Object inst) {
            method.setAccessible(true);
            try {
                return method.invoke(inst);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new VerifyException("cannot get " + method, e);
            }
        }
    }
}

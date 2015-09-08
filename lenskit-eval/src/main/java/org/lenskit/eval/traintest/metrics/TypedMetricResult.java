package org.lenskit.eval.traintest.metrics;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        List<MetricColumn> columns = Lists.newArrayList();
        for (Method m: type.getDeclaredMethods()) {
            MetricColumn info = m.getAnnotation(MetricColumn.class);
            if (info != null) {
                columns.add(info);
            }
        }
        for (Field f: type.getDeclaredFields()) {
            MetricColumn info = f.getAnnotation(MetricColumn.class);
            if (info != null) {
                columns.add(info);
            }
        }

        ImmutableList<MetricColumn> sorted =
                Ordering.natural()
                        .onResultOf(new Function<MetricColumn, Integer>() {
                            @Override
                            public Integer apply(@Nullable MetricColumn input) {
                                assert input != null;
                                int c = input.order();
                                // negative values should sort last
                                return c >= 0 ? c : Integer.MAX_VALUE;
                            }
                        })
                        .immutableSortedCopy(columns);
        return Lists.transform(sorted, new Function<MetricColumn, String>() {
            @Nullable
            @Override
            public String apply(@Nullable MetricColumn input) {
                assert input != null;
                if (suffix == null) {
                    return input.value();
                } else {
                    return input.value() + "." + suffix;
                }
            }
        });
    }

    @Override
    public Map<String, Object> getValues() {
        Map<String,Object> values = new HashMap<>();

        for (Method m: getClass().getDeclaredMethods()) {
            MetricColumn info = m.getAnnotation(MetricColumn.class);
            if (info != null) {
                try {
                    m.setAccessible(true);
                    values.put(info.value(), m.invoke(this));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("cannot invoke " + m, e);
                }
            }
        }

        for (Field f: getClass().getDeclaredFields()) {
            MetricColumn info = f.getAnnotation(MetricColumn.class);
            if (info != null) {
                try {
                    f.setAccessible(true);
                    values.put(info.value(), f.get(this));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("cannot get " + f, e);
                }
            }
        }

        return values;
    }
}

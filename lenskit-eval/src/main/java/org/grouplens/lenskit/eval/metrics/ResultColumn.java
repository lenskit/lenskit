package org.grouplens.lenskit.eval.metrics;

import java.lang.annotation.*;

/**
 * Specify the information for a column method in a metric response type.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 2.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ResultColumn {
    /**
     * The name of this column.
     * @return The column name to use.
     */
    String value();

    /**
     * The order of this column.  A negative order indicates no preference.  All unordered columns
     * come after all ordered columns.
     * @return The column order.
     */
    int order() default -1;
}

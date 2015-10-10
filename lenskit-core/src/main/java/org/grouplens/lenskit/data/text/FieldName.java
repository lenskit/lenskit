package org.grouplens.lenskit.data.text;

import java.lang.annotation.*;

/**
 * Define a field name for a method.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FieldName {
    /**
     * The field name.
     * @return The field name.
     */
    String value();
}

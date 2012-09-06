package org.grouplens.lenskit.core;

import java.lang.annotation.*;

/**
 * Annotation for component parameters. A parameter is a simple value, usually a primitive,
 * string (discouraged), or enum. Parameters have a shortcut configuration syntax.
 * @author Michael Ekstrand
 * @since 1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Parameter {
    /**
     * The parameter's type (e.g. {@code Double.class}).
     * @return The type of the parameter.
     */
    Class<?> value();
}

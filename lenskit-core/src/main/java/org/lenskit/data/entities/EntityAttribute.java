package org.lenskit.data.entities;

import java.lang.annotation.*;

/**
 * Marker for entity attribute methods.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EntityAttribute {
    String value();
}

package org.grouplens.reflens.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provide the default <code>double</code> value for a parameter.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultDouble {
	double value();
}

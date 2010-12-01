package org.grouplens.reflens.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies the default value for a parameter (in the default modules).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
	String value();
}

package org.grouplens.reflens.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Specifies the default implementer for an object-valued parameter.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultClass {
	@SuppressWarnings("unchecked")
	Class value();
}

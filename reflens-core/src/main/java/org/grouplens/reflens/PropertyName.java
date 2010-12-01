package org.grouplens.reflens;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation describing Java properties to be read for the annotation.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyName {
	/**
	 * The name of the Java property for this parameter.
	 * @return
	 */
	String value();
}

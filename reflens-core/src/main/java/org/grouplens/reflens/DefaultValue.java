package org.grouplens.reflens;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
	String value();
}

package org.grouplens.reflens.params.meta;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for Java property names corresponding to a parameter.
 * 
 * For annotations used to identify injected parameters, this meta-annotation
 * records the name of the Java property used to configure that annotation
 * at run time in the default modules.
 * 
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

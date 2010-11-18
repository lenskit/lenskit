/**
 * 
 */
package org.grouplens.reflens.item.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation for a baseline predictor parameter.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaselinePredictor {
	public static final String PROPERTY_NAME =
		"org.grouplens.reflens.item.BaselinePredictor";
}

package org.grouplens.reflens.knn.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.reflens.params.meta.DefaultInt;
import org.grouplens.reflens.params.meta.Parameter;

import com.google.inject.BindingAnnotation;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter
@DefaultInt(50)
public @interface SignificanceThreshold {
}

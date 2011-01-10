package org.grouplens.reflens.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.reflens.params.meta.DefaultValue;
import org.grouplens.reflens.params.meta.PropertyName;

import com.google.inject.BindingAnnotation;

/**
 * Parameter to damp means as recommended by Simon Funk.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyName("reflens.MeanDamping")
@DefaultValue("0")
public @interface MeanDamping {

}

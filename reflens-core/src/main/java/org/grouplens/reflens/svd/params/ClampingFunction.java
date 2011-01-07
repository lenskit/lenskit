package org.grouplens.reflens.svd.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.reflens.params.meta.DefaultClass;
import org.grouplens.reflens.params.meta.PropertyName;
import org.grouplens.reflens.util.DoubleFunction;

import com.google.inject.BindingAnnotation;

@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyName("org.grouplens.reflens.svd.ClampingFunction")
@DefaultClass(DoubleFunction.Identity.class)
public @interface ClampingFunction {

}

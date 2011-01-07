package org.grouplens.reflens.params;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.reflens.params.meta.DefaultValue;
import org.grouplens.reflens.params.meta.PropertyName;

import com.google.inject.BindingAnnotation;

@BindingAnnotation
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@PropertyName("org.grouplens.reflens.MinRating")
@DefaultValue("1")
public @interface MinRating {

}

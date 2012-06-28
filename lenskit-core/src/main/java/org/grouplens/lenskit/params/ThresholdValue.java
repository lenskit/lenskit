package org.grouplens.lenskit.params;

import org.grouplens.grapht.annotation.DefaultDouble;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Threshold below which threshold functions reject similarity values
 * from similarity models.
 */
@Documented
@DefaultDouble(0.0)
@Qualifier
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ThresholdValue {}

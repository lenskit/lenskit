package org.lenskit.slim;

import org.grouplens.grapht.annotation.DefaultBoolean;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Whether or not including intercept in linear regression.
 */
@Documented
@DefaultBoolean(false)
@Parameter(Boolean.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Intercept {
}

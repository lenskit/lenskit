package org.lenskit.pf;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@DefaultDouble(0.3)
@Parameter(Double.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface HyperParameterAPrime {
}
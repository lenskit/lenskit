package org.lenskit.slim;


import org.grouplens.grapht.annotation.DefaultDouble;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * The L2-norm regularization factor (beta) applied in many iterative methods.
 */
@Documented
@DefaultDouble(3)
@Parameter(Double.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RidgeRegressionTerm {
}

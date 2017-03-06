package org.lenskit.slim;


import org.grouplens.grapht.annotation.DefaultImplementation;

import javax.inject.Qualifier;
import java.lang.annotation.*;


@Documented
@Qualifier
@DefaultImplementation(CovarianceUpdateCoordDestLinearRegression.class)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LinearRegression {
}

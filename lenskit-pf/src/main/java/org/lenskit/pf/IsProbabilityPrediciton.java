package org.lenskit.pf;

import org.grouplens.grapht.annotation.DefaultBoolean;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@DefaultBoolean(true)
@Parameter(Boolean.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IsProbabilityPrediciton {
}

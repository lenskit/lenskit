package org.lenskit.pf;

import org.grouplens.grapht.annotation.DefaultInteger;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * The number of latent features to use in an SVD-based recommender.
 */
@Documented
@DefaultInteger(100)
@Parameter(Integer.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureCount {
}

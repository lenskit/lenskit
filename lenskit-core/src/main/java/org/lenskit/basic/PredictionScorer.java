package org.lenskit.basic;

import org.grouplens.grapht.annotation.AllowUnqualifiedMatch;
import org.grouplens.grapht.annotation.DefaultProvider;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Qualifier for item scorer used to produce rating predictions by {@link SimpleRatingPredictor}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Documented
@Qualifier
@AllowUnqualifiedMatch
@DefaultProvider(value = FallbackItemScorer.DynamicProvider.class, skipIfUnusable = true)
public @interface PredictionScorer {
}

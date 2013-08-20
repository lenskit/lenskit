package org.grouplens.lenskit.baseline;

import org.grouplens.grapht.annotation.DefaultNull;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for the baseline scorer of a stacked item scorer, or an item scorer used as a baseline
 * in another component.  A baseline scorer should generally have full coverage (be able to predict
 * for any item).
 *
 * @since 2.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Documented
@Qualifier
@DefaultNull
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BaselineScorer {
}

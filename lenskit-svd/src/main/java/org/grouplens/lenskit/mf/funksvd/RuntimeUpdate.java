package org.grouplens.lenskit.mf.funksvd;

import org.grouplens.grapht.annotation.DefaultNull;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Qualifier for the update rule used at runtime.  This update rule is used by the {@linkplain
 * FunkSVDItemScorer item scorer} to train up user preferences at score time, to run on a more
 * updated version of their profile than was available at the last model build.  By default, no
 * score-time updating is done.
 *
 * @since 1.1
 */
@Documented
@Qualifier
@DefaultNull
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface RuntimeUpdate {
}

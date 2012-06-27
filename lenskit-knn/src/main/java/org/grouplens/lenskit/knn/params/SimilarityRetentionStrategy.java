package org.grouplens.lenskit.knn.params;

import org.grouplens.grapht.annotation.DefaultInteger;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Describes the desired retention strategy to use when
 * considering similarity values.
 * Accepted values:
 *  0 : Retain similarities whose real values are above
 *      the SimilarityThreshold.
 *  1 : Retain similarities whose absolute value are
 *      above the SimilarityThreshold.
 */
@Documented
@DefaultInteger(0)
@Qualifier
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SimilarityRetentionStrategy {}

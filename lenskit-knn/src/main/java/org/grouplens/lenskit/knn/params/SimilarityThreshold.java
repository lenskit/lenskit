package org.grouplens.lenskit.knn.params;

import org.grouplens.grapht.annotation.DefaultDouble;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Threshold below which similarity values are rejected from similarity
 * models. Defaults to only accepting positive similarity values.
 */
@Documented
@DefaultDouble(0.0)
@Qualifier
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface SimilarityThreshold {}

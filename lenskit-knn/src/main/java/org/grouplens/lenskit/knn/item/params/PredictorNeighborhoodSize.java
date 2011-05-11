package org.grouplens.lenskit.knn.item.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.lenskit.params.meta.DefaultInt;
import org.grouplens.lenskit.params.meta.Parameter;

@Documented
@DefaultInt(30)
@Parameter(Integer.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface PredictorNeighborhoodSize { }

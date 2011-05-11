package org.grouplens.lenskit.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.GlobalMeanPredictor;
import org.grouplens.lenskit.params.meta.DefaultClass;
import org.grouplens.lenskit.params.meta.Parameter;

@Documented
@DefaultClass(GlobalMeanPredictor.class)
@Parameter(BaselinePredictor.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface Baseline { }

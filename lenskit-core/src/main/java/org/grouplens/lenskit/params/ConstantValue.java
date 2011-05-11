package org.grouplens.lenskit.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.grouplens.lenskit.params.meta.DefaultDouble;
import org.grouplens.lenskit.params.meta.Parameter;

@Documented
@DefaultDouble(0.0)
@Parameter(Double.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface ConstantValue { }

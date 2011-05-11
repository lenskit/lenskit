package org.grouplens.lenskit.params;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import org.grouplens.lenskit.norm.IdentityUserRatingVectorNormalizer;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;
import org.grouplens.lenskit.params.meta.DefaultClass;
import org.grouplens.lenskit.params.meta.Parameter;

@Documented
@DefaultClass(IdentityUserRatingVectorNormalizer.class)
@Parameter(UserRatingVectorNormalizer.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER })
public @interface Normalizer { }

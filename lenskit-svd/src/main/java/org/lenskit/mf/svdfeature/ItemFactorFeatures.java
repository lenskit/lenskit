package org.lenskit.mf.svdfeature;

import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;
import java.util.Set;

@Documented
@Parameter(Set.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ItemFactorFeatures {
}

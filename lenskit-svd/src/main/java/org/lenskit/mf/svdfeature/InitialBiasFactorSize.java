package org.lenskit.mf.svdfeature;

import org.grouplens.grapht.annotation.DefaultInteger;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@DefaultInteger(100)
@Parameter(Integer.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface InitialBiasFactorSize {
}

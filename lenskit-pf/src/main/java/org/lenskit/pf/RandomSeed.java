package org.lenskit.pf;

import org.grouplens.grapht.annotation.DefaultInteger;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@DefaultInteger(0)
@Parameter(Integer.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface RandomSeed {
}

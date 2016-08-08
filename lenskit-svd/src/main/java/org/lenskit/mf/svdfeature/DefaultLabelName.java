package org.lenskit.mf.svdfeature;

import org.grouplens.grapht.annotation.DefaultString;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@DefaultString("rating")
@Parameter(String.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultLabelName {
}

package org.lenskit.mf.svdfeature;

import org.lenskit.data.entities.EntityType;
import org.lenskit.inject.Parameter;

import javax.inject.Qualifier;
import java.lang.annotation.*;

@Documented
@Parameter(EntityType.class)
@Qualifier
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SVDFeatureEntityType {
}

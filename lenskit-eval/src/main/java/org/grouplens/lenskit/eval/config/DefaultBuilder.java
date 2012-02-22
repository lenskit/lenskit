package org.grouplens.lenskit.eval.config;

import org.apache.commons.lang3.builder.Builder;

import java.lang.annotation.*;

/**
 * Specify the builder for the default type of this class/interface to which
 * it is applied. Used to build objects when the user doesn't specify the
 * particular builder factory to use.
 * @author Michael Ekstrand
 * @since 0.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
// FIXME Make this apply to methods as well
@Target(ElementType.TYPE)
public @interface DefaultBuilder {
    Class<? extends Builder> value();
}

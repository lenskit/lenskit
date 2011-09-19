package org.grouplens.lenskit.util.spi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify a short name that can be used to select a class with
 * {@link ServiceFinder}.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigAlias {
    /**
     * The short configuration name for this implementation.
     */
    public String value();
}

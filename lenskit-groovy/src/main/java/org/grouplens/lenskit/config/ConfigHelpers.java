package org.grouplens.lenskit.config;

import groovy.lang.Closure;
import org.grouplens.lenskit.core.LenskitConfiguration;

/**
 * LensKit configuration helper utilities.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigHelpers {
    /**
     * Load a LensKit configuration from a Groovy closure.  This is useful for using the Groovy
     * DSL in unit tests.
     *
     * @param block The block to evaluate.  This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link groovy.lang.Closure#DELEGATE_FIRST} resolution strategy.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(Closure<?> block) {
        return new ConfigurationLoader().load(block);
    }
}

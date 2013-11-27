package org.grouplens.lenskit.core;

/**
 * Control when a recommender engine is validated.  Validation makes sure that all placeholders
 * have been resolved.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum EngineValidationMode {
    /**
     * Validate the recommender engine when it is loaded.
     */
    IMMEDIATE,
    /**
     * Defer validation until a recommender is created.
     */
    DEFERRED,
    /**
     * Do not validate the recommender engine.  In this case, instantiation may fail arbitrarily
     * if the graph is not valid.
     */
    NONE
}

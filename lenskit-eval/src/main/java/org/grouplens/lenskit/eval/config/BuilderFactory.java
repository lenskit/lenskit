package org.grouplens.lenskit.eval.config;

import org.apache.commons.lang3.builder.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Factort to create builders of particular evaluator components.  Builder factories
 * are registered with the SPI and used to create builders which are then actually
 * used in configuration.
 *
 * @author Michael Ekstrand
 * @param <T> The type of objects built by builders from this factory.
 */
public interface BuilderFactory<T> {
    /**
     * Get the name of this factory, used to invoke it in configuration files.
     * @return The name by which builders created by this factory can be referenced
     * in configuration files.
     */
    String getName();

    /**
     * Create a new builder.
     * @param arg An argument passed when creating the builder.
     * @return A new builder to build objects of the type this factory is attached
     * to.
     */
    @Nonnull
    Builder<T> newBuilder(@Nullable String arg);
}

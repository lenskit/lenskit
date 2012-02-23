package org.grouplens.lenskit.eval.config

import org.apache.commons.lang3.builder.Builder

/**
 * Helper methods for invoking configuration methods.
 * @author Michael Ekstrand
 * @since 0.10
 */
class ConfigHelpers {
    /**
     * Invoke a builder factory to create an object.
     * @param factory The builder factory.
     * @param arg The builder argument (can be {@code null})
     * @param closure A closure to configure the builder
     * @return The built object
     */
    static <T> T invokeBuilderFromFactory(BuilderFactory<T> factory, String arg, Closure closure) {
        def bld = factory.newBuilder(arg)
        invokeBuilder(bld, closure)
    }

    /**
     * Invoke a builder to configure an object.
     * @param builder The builder to use.
     * @param closure A closure to configure the builder. Can be {@code null}. If non-null,
     * this closure is invoked with a {@link BuilderDelegate} to configure the builder.
     * @return The object resulting from the builder.
     */
    static <T> T invokeBuilder(Builder<T> builder, Closure closure) {
        if (closure != null) {
            def delegate = new BuilderDelegate<T>(builder)
            delegate.apply(closure)
        }
        builder.build()
    }
}

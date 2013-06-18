package org.grouplens.lenskit.config;

import groovy.lang.Closure;
import org.grouplens.lenskit.core.LenskitConfiguration;

/**
 * Load LensKit configurations using the configuration DSL.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ConfigurationLoader {
    private final ClassLoader classLoader;

    /**
     * Construct a new configuration loader. It uses the current thread's class loader.
     * @review Is this the classloader we should use?
     */
    public ConfigurationLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Construct a new configuration loader.
     * @param loader The class loader to use.
     */
    public ConfigurationLoader(ClassLoader loader) {
        classLoader = loader;
    }

    /**
     * Load a configuration from a closure. The class loader is not really consulted in this case.
     * @param block The block to evaluate. This block will be evaluated with a delegate providing
     *              the LensKit DSL and the {@link Closure#DELEGATE_FIRST} resolution strategy.
     * @return The resulting LensKit configuration.
     */
    public LenskitConfiguration load(Closure<?> block) {
        LenskitConfiguration config = new LenskitConfiguration();
        BindingDSL delegate = new LenskitConfigDSL(config);
        block.setDelegate(delegate);
        block.setResolveStrategy(Closure.DELEGATE_FIRST);
        block.call();
        return config;
    }
}

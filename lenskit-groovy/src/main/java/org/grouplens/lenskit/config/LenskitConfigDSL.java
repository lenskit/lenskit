package org.grouplens.lenskit.config;

import org.grouplens.lenskit.core.LenskitConfiguration;

/**
 * Methods for the LensKit configuration DSL.  This extends {@link BindingDSL} with additional
 * methods available at the top level for configuring the LensKit configuration itself.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitConfigDSL extends BindingDSL {
    private final LenskitConfiguration config;

    /**
     * Construct a new delegate with an empty configuration.
     */
    public LenskitConfigDSL() {
        this(new LenskitConfiguration());
    }

    /**
     * Construct a new delegate.
     *
     * @param cfg The context to configure.
     */
    LenskitConfigDSL(LenskitConfiguration cfg) {
        super(cfg);
        config = cfg;
    }

    /**
     * Get the LensKit configuration being configured.
     * @return The configuration that this delegate configures.
     */
    public LenskitConfiguration getConfig() {
        return config;
    }

    /**
     * Add a root type.
     * @param type The type to add.
     * @see LenskitConfiguration#addRoot(Class)
     */
    public void root(Class<?> type) {
        config.addRoot(type);
    }
}

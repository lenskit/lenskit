/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.lenskit.config;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.SystemUtils;
import org.lenskit.LenskitConfiguration;
import org.lenskit.RecommenderConfigurationException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Methods for the LensKit configuration DSL.  This extends {@link BindingDSL} with additional
 * methods available at the top level for configuring the LensKit configuration itself.
 *
 * @since 1.2
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class LenskitConfigDSL extends BindingDSL {
    private final LenskitConfiguration config;
    private final ConfigurationLoader configLoader;
    private URI baseURI;

    /**
     * Construct a new delegate with an empty configuration.
     *
     * @param loader The configuration loader.
     */
    public LenskitConfigDSL(ConfigurationLoader loader) {
        this(loader, null);
    }

    /**
     * Construct a new delegate with an empty configuration.
     *
     * @param loader The configuration loader
     * @param base   The base URL
     */
    public LenskitConfigDSL(ConfigurationLoader loader, URI base) {
        this(loader, new LenskitConfiguration(), base);
    }

    /**
     * Construct a new delegate.
     *
     * @param loader The configuration loader.
     * @param cfg    The context to configure.
     * @param base   The base URL.
     */
    protected LenskitConfigDSL(ConfigurationLoader loader, LenskitConfiguration cfg, URI base) {
        super(cfg);
        config = cfg;
        configLoader = loader;
        if (base == null) {
            baseURI = new File(System.getProperty("user.dir")).toURI();
        } else {
            baseURI = base;
        }
    }

    static LenskitConfigDSL forConfig(LenskitConfiguration cfg) {
        return new LenskitConfigDSL(null, cfg, null);
    }

    /**
     * Get the LensKit configuration being configured.
     * @return The configuration that this delegate configures.
     */
    public LenskitConfiguration getConfig() {
        return config;
    }

    /**
     * Get the configuration loader associated with this DSL (used to power the {@link #include(java.io.File)}
     * method).
     * @throws IllegalStateException if there is no associated loader.
     */
    public ConfigurationLoader getConfigLoader() {
        if (configLoader == null) {
            throw new IllegalStateException("no configuration loader specified");
        }
        return configLoader;
    }

    /**
     * Get the base URL for this configuration.
     * @return The base URI.
     */
    @Nonnull
    public URI getBaseURI() {
        if (baseURI == null) {
            return SystemUtils.getUserDir().toURI();
        } else {
            return baseURI;
        }
    }

    /**
     * Set the base URI for this configuration.
     * @param base The base URI.
     */
    public void setBaseURI(URI base) {
        baseURI = base;
    }

    /**
     * Add a root type.
     * @param type The type to add.
     * @see LenskitConfiguration#addRoot(Class)
     */
    public void root(Class<?> type) {
        config.addRoot(type);
    }

    // Override to make it actually work
    @Override
    public void include(URI uri) throws IOException, RecommenderConfigurationException {
        URI realURI = uri;
        if (!realURI.isAbsolute()) {
            realURI = getBaseURI().resolve(realURI);
        }
        LenskitConfigScript script = getConfigLoader().loadScript(realURI.toURL());
        script.configure(getConfig());
    }
}

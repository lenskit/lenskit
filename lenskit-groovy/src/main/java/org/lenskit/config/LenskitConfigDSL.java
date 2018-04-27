/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.config;

import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.data.pref.PreferenceDomainBuilder;

import java.util.Map;

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

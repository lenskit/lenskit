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

import groovy.lang.Closure;
import org.grouplens.lenskit.core.LenskitConfiguration;
import org.grouplens.lenskit.core.RecommenderConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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
     * @see ConfigurationLoader#load(groovy.lang.Closure)
     */
    public static LenskitConfiguration load(Closure<?> block) throws RecommenderConfigurationException {
        return new ConfigurationLoader().load(block);
    }

    /**
     * Load a LensKit configuration from a script (as a string).
     *
     * @param script The script source text to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(String script) throws RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script file.
     *
     * @param script The script source file to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(File script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }

    /**
     * Load a LensKit configuration from a script URL.
     *
     * @param script The script source URL to evaluate.
     * @return The LensKit configuration.
     */
    public static LenskitConfiguration load(URL script) throws IOException, RecommenderConfigurationException {
        return new ConfigurationLoader().load(script);
    }
}

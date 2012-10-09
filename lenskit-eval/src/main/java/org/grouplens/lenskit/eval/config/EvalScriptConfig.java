/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.config;

import java.util.Properties;

/**
 * The class that represents the set of properties passed in to a
 * Groovy evaluation script.  These properties will have been created
 * a caller, who passes in a Properties object, which will be embedded
 * in the instance of this class.  This instance will then be passed
 * in to the Groovy evaluation script under a special name, so the
 * script can use it to pull properties out.
 *
 * @author John Riedl
 * @since 1.1
 */
public class EvalScriptConfig {
    private Properties properties;

    public EvalScriptConfig() {
        this(new Properties());
    }

    public EvalScriptConfig(Properties props) {
        properties = (Properties) props.clone();
    }

    /**
     * Get the value of a property.
     *
     * @param key          The name of the property
     * @param defaultValue The value to return if no such key
     * @return The value of the property
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get the value of a property.
     *
     * @param key The name of the property
     * @return The value of the property, or null if there is no such key
     */
    public String getProperty(String key) {
        return properties.getProperty(key, null);
    }

    /**
     * Get the script for this evaluation.  Will often include a path.
     *
     * @return The script name, or "eval.groovy" if none has been set.
     */
    public String getScript() {
        return getProperty("lenskit.eval.script", "eval.groovy");
    }

    /**
     * Get the data directory for this evaluation.
     *
     * @return The data directory, or "." if none has been set.
     */
    public String getDataDir() {
        return getProperty("lenskit.eval.dataDir", ".");
    }

    /**
     * Get the analysis directory for this evaluation.
     *
     * @return The analysis directory, or "." if none has been set.
     */
    public String getAnalysisDir() {
        return getProperty("lenskit.eval.analysisDir", ".");
    }

}

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
package org.grouplens.lenskit.eval.config;

import org.apache.commons.lang3.BooleanUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * The class that represents the set of properties passed in to a
 * Groovy evaluation script.  These properties will have been created
 * by a caller, who passes in a Properties object, which will be embedded
 * in the instance of this class.  This instance will then be passed
 * in to the Groovy evaluation script under a special name, so the
 * script can use it to pull properties out.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public class EvalConfig {
    public static final String FORCE_PROPERTY = "lenskit.eval.force";
    public static final String SKIP_PROPERTY = "lenskit.eval.skip";
    public static final String EVAL_SCRIPT_PROPERTY = "lenskit.eval.script";
    public static final String EVAL_SCRIPTFILES_PROPERTY = "lenskit.eval.scripts";
    public static final String DATA_DIR_PROPERTY = "lenskit.eval.dataDir";
    public static final String ANALYSIS_DIR_PROPERTY = "lenskit.eval.analysisDir";
    public static final String THREAD_COUNT_PROPERTY = "lenskit.eval.threadCount";

    private final Map<String, String> properties;

    /**
     * Construct a new eval config using the specified properties.
     * @param props The properties to use.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public EvalConfig(@Nonnull Map props) {
        properties = props;
    }

    /**
     * Get the value of a property.
     *
     * @param key          The name of the property
     * @param defaultValue The value to return if no such key
     * @return The value of the property
     */
    public String get(String key, @Nullable String defaultValue) {
        String val = properties.get(key);
        if (val == null) {
            return defaultValue;
        } else {
            return val;
        }
    }

    /**
     * Get the value of a property.
     *
     * @param key The name of the property
     * @return The value of the property, or {@code null} if there is no such key
     */
    public String get(String key) {
        return get(key, null);
    }

    /**
     * Query whether the script should run in “force” mode.
     *
     * @return {@code true} if the script should run in force mode.
     */
    public boolean force() {
        return BooleanUtils.toBoolean(get(FORCE_PROPERTY));
    }

    /**
     * Get the script for this evaluation.  Will often include a path.
     *
     * @return The script name, or "eval.groovy" if none has been set.
     */
    public String getScript() {
        return get(EVAL_SCRIPT_PROPERTY, "eval.groovy");
    }

    /**
     * Get the data directory for this evaluation.
     *
     * @return The data directory, or "." if none has been set.
     */
    public String getDataDir() {
        return get(DATA_DIR_PROPERTY, ".");
    }

    /**
     * Get the analysis directory for this evaluation.
     *
     * @return The analysis directory, or "." if none has been set.
     */
    public String getAnalysisDir() {
        return get(ANALYSIS_DIR_PROPERTY, ".");
    }

    public int getThreadCount() {
        int count = Integer.parseInt(get(THREAD_COUNT_PROPERTY, "1"));
        if (count == 0) {
            return Runtime.getRuntime().availableProcessors();
        } else {
            return count;
        }
    }
}

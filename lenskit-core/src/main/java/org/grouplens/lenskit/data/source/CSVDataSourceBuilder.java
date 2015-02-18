/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.grouplens.lenskit.data.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Simplified builder that returns a CSV data source (or delimited with another delimiter).
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CSVDataSourceBuilder extends TextDataSourceBuilder {
    private static final Logger logger = LoggerFactory.getLogger(CSVDataSourceBuilder.class);

    public CSVDataSourceBuilder() {}

    public CSVDataSourceBuilder(String name) {
        super(name);
    }

    public CSVDataSourceBuilder(File file) {
        super(file);
    }

    /**
     * Specify whether to cache ratings in memory. Caching is enabled by default.
     *
     * @param on {@code false} to disable caching.
     */
    @Deprecated
    public CSVDataSourceBuilder setCache(boolean on) {
        logger.warn("the cache directive on CSV files is now a no-op");
        return this;
    }
}

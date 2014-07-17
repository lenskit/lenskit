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
package org.grouplens.lenskit.eval.data;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Command to return a CSV data source.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CSVDataSourceBuilder implements Builder<DataSource> {
    private static final Logger logger = LoggerFactory.getLogger(CSVDataSourceBuilder.class);
    private String name;
    String delimiter = ",";
    File inputFile;
    PreferenceDomain domain;

    public CSVDataSourceBuilder() {}

    public CSVDataSourceBuilder(String name) {
        this.name = name;
    }

    public CSVDataSourceBuilder(File file) {
        inputFile = file;
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     *
     * @param n The name of the data source.
     * @see #setFile(File)
     */
    public CSVDataSourceBuilder setName(String n) {
        name = n;
        return this;
    }

    @Nonnull
    public String getName() {
        if (name != null) {
            return name;
        } else {
            return inputFile.getName();
        }
    }

    /**
     * Get the input file.
     * @return The input file for the builder.
     */
    public File getFile() {
        return inputFile;
    }

    /**
     * Set the input file. If unspecified, the name (see {@link #setName(String)}) is used
     * as the file name.
     *
     * @param file The file to read ratings from.
     */
    public CSVDataSourceBuilder setFile(File file) {
        inputFile = file;
        return this;
    }

    /**
     * Set the input file by name.
     * @param fn The input file name.
     * @return The builder (for chaining).
     */
    public CSVDataSourceBuilder setFile(String fn) {
        return setFile(new File(fn));
    }

    /**
     * Get the input delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Set the input field delimiter. The default is the tab character.
     *
     * @param delim The input delimiter.
     */
    public CSVDataSourceBuilder setDelimiter(String delim) {
        delimiter = delim;
        return this;
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

    public PreferenceDomain getDomain() {
        return domain;
    }

    /**
     * Set the preference domain for the data source.
     *
     * @param dom The preference domain.
     * @return The command (for chaining).
     */
    public CSVDataSourceBuilder setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    /**
     * Build the data source. At least one of {@link #setName(String)} or
     * {@link #setFile(File)} must be called prior to building.
     *
     * @return The configured data source.
     */
    @Override
    public CSVDataSource build() {
        // if no name, use the file name
        if (name == null && inputFile != null) {
            setName(inputFile.toString());
        }
        // if no file, use the name
        if (inputFile == null && name != null) {
            inputFile = new File(getName());
        }
        // by now we should have a file
        Preconditions.checkState(inputFile != null, "no input file specified");
        return new CSVDataSource(getName(), inputFile, delimiter, domain);
    }
}

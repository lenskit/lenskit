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
package org.grouplens.lenskit.eval.data;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.grouplens.lenskit.data.dao.DAOFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AbstractCommand;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * Command to return a CSV data source.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class CSVDataSourceCommand extends AbstractCommand<CSVDataSource> {
    String delimiter = ",";
    File inputFile;
    boolean cache = true;
    PreferenceDomain domain;
    Function<DAOFactory, DAOFactory> wrapper;

    public CSVDataSourceCommand() {
        this(null);
    }

    public CSVDataSourceCommand(String name) {
        super(name);
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     *
     * @param name The name of the data source.
     * @see #setFile(File)
     */
    @Override
    public CSVDataSourceCommand setName(String name) {
        super.setName(name);
        return this;
    }

    @Nonnull
    @Override
    public String getName() {
        if (hasName()) {
            return super.getName();
        } else {
            return inputFile.getName();
        }
    }

    /**
     * Set the input file. If unspecified, the name (see {@link #setName(String)}) is used
     * as the file name.
     *
     * @param file The file to read ratings from.
     */
    public CSVDataSourceCommand setFile(File file) {
        inputFile = file;
        return this;
    }

    /**
     * Set the input field delimiter. The default is the tab character.
     *
     * @param delim The input delimiter.
     */
    public CSVDataSourceCommand setDelimiter(String delim) {
        delimiter = delim;
        return this;
    }

    /**
     * Specify whether to cache ratings in memory. Caching is enabled by default.
     *
     * @param on {@code false} to disable caching.
     */
    public CSVDataSourceCommand setCache(boolean on) {
        cache = on;
        return this;
    }

    /**
     * Set the preference domain for the data source.
     *
     * @param dom The preference domain.
     * @return The command (for chaining).
     */
    public CSVDataSourceCommand setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    /**
     * Set a wrapper function to apply to the resulting DAOs. The data source command
     * wraps its DAO with this function, allowing it to be augmented with additional
     * information or transformations if desired.
     *
     * @param wrapFun The DAO wrapper function.
     * @return The command (for chaining).
     */
    public CSVDataSourceCommand setWrapper(Function<DAOFactory, DAOFactory> wrapFun) {
        wrapper = wrapFun;
        return this;
    }

    /**
     * Build the data source. At least one of {@link #setName(String)} or
     * {@link #setFile(File)} must be called prior to building.
     *
     * @return The configured data source.
     */
    @Override
    public CSVDataSource call() {
        // if no name, use the file name
        if (!hasName() && inputFile != null) {
            setName(inputFile.toString());
        }
        // if no file, use the name
        if (inputFile == null && hasName()) {
            inputFile = new File(getName());
        }
        // by now we should have a file
        Preconditions.checkState(inputFile != null, "no input file specified");
        return new CSVDataSource(getName(), inputFile, delimiter, cache, domain, wrapper);
    }
}

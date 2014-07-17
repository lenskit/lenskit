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
package org.grouplens.lenskit.eval.data.pack;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class PackedDataSourceBuilder implements Builder<PackedDataSource> {
    private File file;
    private PreferenceDomain domain;
    private String name;

    public PackedDataSourceBuilder() {}

    public PackedDataSourceBuilder(String name) {
        this.name = name;
    }

    public PackedDataSourceBuilder(File f) {
        file = f;
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     *
     * @param n The name of the data source.
     * @see #setFile(File)
     */
    public PackedDataSourceBuilder setName(String n) {
        name = n;
        return this;
    }

    @Nonnull
    public String getName() {
        if (name != null) {
            return name;
        } else {
            return file.getName();
        }
    }

    /**
     * Get the input file.
     * @return The input file for the builder.
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the input file. If unspecified, the name (see {@link #setName(String)}) is used
     * as the file name.
     *
     * @param f The file to read ratings from.
     */
    public PackedDataSourceBuilder setFile(File f) {
        file = f;
        return this;
    }

    /**
     * Set the input file by name.
     *
     * @param fn The input file name.
     * @return The builder (for chaining).
     */
    public PackedDataSourceBuilder setFile(String fn) {
        return setFile(new File(fn));
    }

    public PreferenceDomain getDomain() {
        return domain;
    }

    /**
     * Set the preference domain for the data source.
     *
     *
     * @param dom The preference domain.
     * @return The command (for chaining).
     */
    public PackedDataSourceBuilder setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    @Override
    public PackedDataSource build() {
        return new PackedDataSource(getName(), file, getDomain());
    }
}

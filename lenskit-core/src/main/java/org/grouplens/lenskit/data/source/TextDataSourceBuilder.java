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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.Builder;
import org.grouplens.lenskit.data.text.DelimitedColumnEventFormat;
import org.grouplens.lenskit.data.text.EventFormat;
import org.lenskit.data.dao.file.DelimitedColumnEntityFormat;
import org.lenskit.data.dao.file.Formats;
import org.lenskit.data.ratings.PreferenceDomain;
import org.lenskit.data.ratings.RatingBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.file.Path;

/**
 * Builder for text file data sources.
 *
 * @since 2.2
 */
public class TextDataSourceBuilder implements Builder<DataSource> {
    private String name;
    File inputFile;
    PreferenceDomain domain;
    DelimitedColumnEntityFormat entityFormat = Formats.csvRatings();
    DelimitedColumnEventFormat dceFormat =
            DelimitedColumnEventFormat.create(RatingBuilder.class)
                                      .setDelimiter(",");
    EventFormat format = dceFormat;
    private Path itemFile;
    private Path itemNameFile;

    public TextDataSourceBuilder() {}

    public TextDataSourceBuilder(String name) {
        this.name = name;
    }

    public TextDataSourceBuilder(File file) {
        inputFile = file;
    }

    /**
     * Set the data source name. If unspecified, a name is derived from the file.
     *
     * @param n The name of the data source.
     * @see #setFile(java.io.File)
     */
    public TextDataSourceBuilder setName(String n) {
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
    public TextDataSourceBuilder setFile(File file) {
        inputFile = file;
        return this;
    }

    /**
     * Set the input file by name.
     * @param fn The input file name.
     * @return The builder (for chaining).
     */
    public TextDataSourceBuilder setFile(String fn) {
        return setFile(new File(fn));
    }

    /**
     * Get the currently-configured event format.
     * @return The configured event format.
     */
    @Deprecated
    public EventFormat getFormat() {
        return format;
    }

    /**
     * Set the event format.  This method overrides any previous calls to set the delimiter or
     * columns, and disables any future calls.
     *
     * @param fmt The format.
     * @return The builder (for chaining).
     */
    @Deprecated
    public TextDataSourceBuilder setFormat(EventFormat fmt) {
        format = fmt;
        dceFormat = null;
        entityFormat = null;
        return this;
    }

    /**
     * Get the input delimiter.
     */
    public String getDelimiter() {
        return entityFormat != null ? entityFormat.getDelimiter() : null;
    }

    /**
     * Set the input field delimiter. The default is the tab character.
     *
     * @param delim The input delimiter.
     * @throws IllegalStateException if a format has been specified with {@link #setFormat(EventFormat)}.
     */
    public TextDataSourceBuilder setDelimiter(String delim) {
        if (dceFormat != null) {
            dceFormat.setDelimiter(delim);
            entityFormat.setDelimiter(delim);
        } else {
            throw new IllegalStateException("event format already specified");
        }
        return this;
    }

    /**
     * Get the number of header lines that will be skipped.
     * @return The number of header lines that will be skipped.
     */
    public int getHeaderLines() {
        return entityFormat.getHeaderLines();
    }

    /**
     * Set the number of header lines to skip.
     * @param lines The number of header lines to skip.
     * @return The builder (for chaining).
     */
    public TextDataSourceBuilder setHeaderLines(int lines) {
        if (dceFormat != null) {
            dceFormat.setHeaderLines(lines);
            entityFormat.setHeaderLines(lines);
        } else {
            throw new IllegalStateException("event format already specified");
        }
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
    public TextDataSourceBuilder setDomain(PreferenceDomain dom) {
        domain = dom;
        return this;
    }

    /**
     * Get the item file.
     * @return The item file, if one is specified.
     */
    public Path getItemFile() {
        return itemFile;
    }

    /**
     * Set the item ID file.
     * @param file The item file, if one is specified.
     */
    public TextDataSourceBuilder setItemFile(Path file) {
        itemFile = file;
        return this;
    }

    /**
     * Set the item ID file.
     * @param file The item file, if one is specified.
     * @deprecated Use {@link #setItemFile(Path)}
     */
    @Deprecated
    public TextDataSourceBuilder setItemFile(File file) {
        Path path = file != null ? file.toPath() : null;
        return setItemFile(path);
    }

    /**
     * Get the item file.
     * @return The item file, if one is specified.
     */
    public Path getItemNameFile() {
        return itemNameFile;
    }

    /**
     * Set the item ID to name mapping file.
     * @param file The item name file, if one is specified.
     */
    public TextDataSourceBuilder setItemNameFile(Path file) {
        itemNameFile = file;
        return this;
    }

    /**
     * Set the item ID to name mapping file.
     * @param file The item name file, if one is specified.
     * @deprecated Use {@link #setItemNameFile(Path)}.
     */
    @Deprecated
    public TextDataSourceBuilder setItemNameFile(File file) {
        Path path = file != null ? file.toPath() : null;
        return setItemNameFile(path);
    }

    /**
     * Build the data source. At least one of {@link #setName(String)} or
     * {@link #setFile(java.io.File)} must be called prior to building.
     *
     * @return The configured data source.
     */
    @Override
    public TextDataSource build() {
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
        if (entityFormat == null) {
            throw new IllegalStateException("other entity formats not supported");
        }
        return new TextDataSource(getName(), inputFile, entityFormat, format, domain, itemFile, itemNameFile);
    }
}

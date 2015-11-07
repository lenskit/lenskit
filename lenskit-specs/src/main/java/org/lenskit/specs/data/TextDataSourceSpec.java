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
package org.lenskit.specs.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;

/**
 * Specification of a text data source.
 */
public class TextDataSourceSpec extends DataSourceSpec {
    private Path file;
    private String delimiter = ",";
    private PrefDomainSpec domain;
    private String builderType = "rating";
    private List<String> fields;
    private Path itemFile;
    private Path itemNameFile;
    private int headerLines;

    @Override
    public String getName() {
        String name = super.getName();
        if (name == null && file != null) {
            return file.getFileName().toString();
        } else {
            return name;
        }
    }

    /**
     * Get the number of header lines to skip.
     * @return the number of header lines to skip.
     */
    public int getHeaderLines() {
        return headerLines;
    }

    /**
     * Set the number of header lines to skip.
     * @param the number of header lines to skip.
     */
    public void setHeaderLines(int headerLines) {
        this.headerLines = headerLines;
    }

    public Path getFile() {
        return file;
    }

    public void setFile(Path file) {
        this.file = file;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Get the event type.
     * @return The name of the event type.
     */
    public String getBuilderType() {
        return builderType;
    }

    /**
     * Set the event type.
     * @param type The name of the event type.
     */
    public void setBuilderType(String type) {
        builderType = type;
    }

    /**
     * Get the list of fields.
     * @return The list of fields in the data source.
     */
    @Nullable
    public List<String> getFields() {
        return fields;
    }
    
    /**
     * Set the list of fields in the data source.
     * @param fields The list of fields.  Can be `null` to use the builder's default fields.
     */
    public void setFields(@Nullable List<String> fields) {
        this.fields = fields != null ? new ArrayList<>(fields) : null;
    }

    public Path getItemFile() {
        return itemFile;
    }
    public void setItemFile(Path file) {
        itemFile = file;
    }

    public Path getItemNameFile() {
        return itemNameFile;
    }

    public void setItemNameFile(Path file) {
        itemNameFile = file;
    }

    @Override
    public PrefDomainSpec getDomain() {
        return domain;
    }

    public void setDomain(PrefDomainSpec domain) {
        this.domain = domain;
    }

    @Override
    public Set<Path> getInputFiles() {
        Set<Path> files = new HashSet<>();
        files.add(file);
        if (itemFile != null) {
            files.add(itemFile);
        }
        if (itemNameFile != null) {
            files.add(itemNameFile);
        }
        return files;
    }
}

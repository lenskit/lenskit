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
package org.lenskit.data.dao.file;

import com.google.common.base.Preconditions;
import org.lenskit.data.entities.Attribute;
import org.lenskit.data.entities.EntityBuilder;
import org.lenskit.data.entities.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delimited text column entity format.
 */
public class DelimitedColumnEntityFormat implements EntityFormat {
    private String delimiter;
    private int headerLines;
    private boolean readHeader;
    private EntityType entityType;
    private Class<? extends EntityBuilder> entityBuilder;
    private List<Attribute<?>> columns;
    private Map<String,Attribute<?>> labeledColumns;

    /**
     * Get the delimiter for the entity format.
     * @return The entity format delimiter.
     */
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Set the delimiter to use for this entity format.
     * @param delim The delimiter to use.
     */
    public void setDelimiter(String delim) {
        delimiter = delim;
    }

    /**
     * Set whether to read a header.
     * @param header `true` to read a header line from the file.
     */
    public void setHeader(boolean header) {
        readHeader = header;
        headerLines = 1;
    }

    /**
     * Get the number of header lines to read.
     * @return The number of header lines to read.
     */
    public int getHeaderLines() {
        return headerLines;
    }

    /**
     * Set the number of header lines to read.  Setting this **disables** {@link #setHeader(boolean)}.
     * @param lines The number of header lines to read.
     */
    public void setHeaderLines(int lines) {
        headerLines = lines;
        readHeader = false;
    }

    /**
     * Set the entity type.
     * @param type The entity type.
     */
    public void setEntityType(EntityType type) {
        entityType = type;
    }

    /**
     * Get the entity type.
     * @return The entity type.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Set the entity builder class.
     * @param builder The entity builder class.
     */
    public void setEntityBuilder(Class<? extends EntityBuilder> builder) {
        entityBuilder = builder;
    }

    /**
     * Get the entity builder class.
     * @return The entity builder class.
     */
    public Class<? extends EntityBuilder> getEntityBuilder() {
        return entityBuilder;
    }

    /**
     * Instantiate a new entity builder.
     * @return A new entity builder.
     */
    public EntityBuilder newEntityBuilder() {
        try {
            return entityBuilder.newInstance().setType(entityType);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("could not instantiate entity builder", e);
        }
    }

    /**
     * Add a column.
     * @param attr The attribute to add as a column, or `null` to skip the next column.
     * @throws IllegalStateException if non-labeled columns have already been specified.
     */
    public void addColumn(@Nullable Attribute<?> attr) {
        if (columns == null) {
            Preconditions.checkState(labeledColumns == null, "mixed labeled and unlabeled columns");
            columns = new ArrayList<>();
        }
        columns.add(attr);
    }

    /**
     * Add a column.
     * @param label The header label.
     * @param attr The attribute to add as a column, or `null` to skip the next column.
     * @throws IllegalStateException if non-labeled columns have already been specified.
     */
    public void addColumn(String label, @Nullable Attribute<?> attr) {
        if (labeledColumns == null) {
            Preconditions.checkState(columns == null, "mixed labeled and unlabeled columns");
            labeledColumns = new LinkedHashMap<>();
        }
        labeledColumns.put(label, attr);
    }
}

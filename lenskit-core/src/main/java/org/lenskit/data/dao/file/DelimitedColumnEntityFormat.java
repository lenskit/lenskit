/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.text.StrTokenizer;
import org.lenskit.data.entities.*;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delimited text column entity format.
 */
public class DelimitedColumnEntityFormat implements EntityFormat {
    private String delimiter = "\t";
    private int headerLines;
    private boolean readHeader;
    private EntityType entityType = EntityType.forName("rating");
    private Class<? extends EntityBuilder> entityBuilder = BasicEntityBuilder.class;
    private Constructor<? extends EntityBuilder> entityBuilderCtor;
    private List<TypedName<?>> columns;
    private Map<String,TypedName<?>> labeledColumns;

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
     * Query whether this format uses the header line(s).
     * @return `true` if the reader will parse a header line.
     */
    boolean usesHeader() {
        return readHeader;
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
    @Override
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
        if (entityBuilderCtor == null || !entityBuilderCtor.getDeclaringClass().equals(entityBuilder)) {
            try {
                entityBuilderCtor = entityBuilder.getConstructor(EntityType.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("cannot find suitable constructor for " + entityBuilder);
            }
        }
        try {
            return entityBuilderCtor.newInstance(entityType);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("could not instantiate entity builder", e);
        }
    }

    /**
     * Associate an attribute with the next column.   The first time this is called it specifies the label for the
     * first column. The second time this is called it specifies the second column label and so forth.
     *
     * Once this method has been called, {@link #addColumn(String, TypedName)} cannot be called.
     *
     * @param attr The attribute to add as a column, or `null` to skip the next column.
     * @throws IllegalStateException if non-labeled columns have already been specified.
     */
    public void addColumn(@Nullable TypedName<?> attr) {
        if (columns == null) {
            Preconditions.checkState(labeledColumns == null, "mixed labeled and unlabeled columns");
            columns = new ArrayList<>();
        }
        columns.add(attr);
    }

    /**
     * Add columns to a format.  This is exactly equivalent to calling {@link #addColumn(TypedName)} for each
     * column.
     *
     * @param columns The columns to add.
     */
    public void addColumns(TypedName<?>... columns) {
        for (TypedName<?> col: columns) {
            addColumn(col);
        }
    }

    /**
     * Get the list of columns, if {@link #addColumn(TypedName)} has been used.
     * @return The list of columns.
     * @throws IllegalStateException if {@link #addColumn(TypedName)} or {@link #addColumns(TypedName[])} have not been
     * called.
     */
    public List<TypedName<?>> getColumnList() {
        Preconditions.checkState(columns != null);
        return ImmutableList.copyOf(columns);
    }

    /**
     * Add a column.
     * @param label The header label.
     * @param attr The attribute to add as a column, or `null` to skip the next column.
     * @throws IllegalStateException if non-labeled columns have already been specified.
     */
    public void addColumn(String label, @Nullable TypedName<?> attr) {
        if (labeledColumns == null) {
            Preconditions.checkState(columns == null, "mixed labeled and unlabeled columns");
            labeledColumns = new LinkedHashMap<>();
        }
        labeledColumns.put(label, attr);
    }

    /**
     * Clear the columns configured for this format.
     */
    public void clearColumns() {
        columns = null;
        labeledColumns = null;
    }

    @Override
    public ObjectNode toJSON() {
        JsonNodeFactory nf = JsonNodeFactory.instance;

        ObjectNode json = nf.objectNode();
        json.put("format", "delimited");
        json.put("delimiter", delimiter);
        json.put("entity_type", entityType.getName());
        if (readHeader) {
            json.put("header", true);
        } else if (headerLines > 0) {
            json.put("header", headerLines);
        }
        if (columns != null) {
            ArrayNode cols = json.putArray("columns");
            for (TypedName<?> col: columns) {
                ObjectNode colObj = cols.addObject();
                colObj.put("name", col.getName());
                colObj.put("type", col.getType().getName());
            }
        } else if (labeledColumns != null) {
            ObjectNode cols = json.putObject("columns");
            for (Map.Entry<String,TypedName<?>> colE: labeledColumns.entrySet()) {
                ObjectNode colNode = cols.putObject(colE.getKey());
                colNode.put("name", colE.getValue().getName());
                colNode.put("type", colE.getValue().getType().getName());
            }
        } else {
            throw new IllegalStateException("no labels specified");
        }

        return json;
    }

    @Override
    public LineEntityParser makeParser(List<String> header) {
        assert header.size() == getHeaderLines();

        if (usesHeader() && labeledColumns != null) {
            assert header.size() == 1;
            List<TypedName<?>> cols = new ArrayList<>();
            StrTokenizer tok = new StrTokenizer(header.get(0), delimiter);
            tok.setQuoteChar('"');
            while (tok.hasNext()) {
                String label = tok.next();
                cols.add(labeledColumns.get(label));
            }
            return new OrderedParser(cols, tok);
        } else {
            Preconditions.checkState(columns != null, "no columns specified");
            StrTokenizer tok = new StrTokenizer("", delimiter);
            tok.setQuoteChar('"');
            return new OrderedParser(columns, tok);
        }
    }

    private class OrderedParser extends LineEntityParser {
        int lineNo = 0;
        StrTokenizer tokenizer;
        List<TypedName<?>> fileColumns;

        public OrderedParser(List<TypedName<?>> columns, StrTokenizer tok) {
            fileColumns = columns;
            tokenizer = tok;
        }

        @Override
        public Entity parse(String line) {
            tokenizer.reset(line);
            lineNo += 1;

            EntityBuilder builder = newEntityBuilder()
                    .setId(lineNo);

            // since ID is already set, a subsequent ID column will properly override

            for (TypedName column: fileColumns) {
                String value = tokenizer.nextToken();
                if (value != null && column != null) {
                    builder.setAttribute(column, column.parseString(value));
                }
            }

            return builder.build();
        }
    }
}

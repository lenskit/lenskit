/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.data.dao.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.text.StringTokenizer;
import org.lenskit.data.dao.DataAccessException;
import org.lenskit.data.entities.*;
import org.lenskit.util.TypeUtils;
import org.lenskit.util.reflect.InstanceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.lenskit.data.dao.file.TextEntitySource.parseAttribute;

/**
 * Delimited text column entity format.
 */
public class DelimitedColumnEntityFormat implements EntityFormat {
    private static final Logger logger = LoggerFactory.getLogger(DelimitedColumnEntityFormat.class);
    private String delimiter = "\t";
    private int headerLines;
    private boolean readHeader;
    private long baseId;
    private EntityType entityType = EntityType.forName("rating");
    private Class<? extends EntityBuilder> entityBuilder = BasicEntityBuilder.class;
    private InstanceFactory<EntityBuilder> builderFactory;
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
     * Get the base entity ID for this source.
     * @return The base entity ID.
     */
    public long getBaseId() {
        return baseId;
    }

    /**
     * Set the base entity ID for this source.  If an entity column is not defined, then the line number will
     * be added to this value to obtain a synthetic ID.
     * @param base The base entity ID.
     */
    public void setBaseId(long base) {
        baseId = base;
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
        builderFactory = null;
    }

    /**
     * Get the entity builder class.
     * @return The entity builder class.
     */
    @Override
    @Nonnull
    public Class<? extends EntityBuilder> getEntityBuilder() {
        return entityBuilder;
    }

    @Nullable
    @Override
    public AttributeSet getAttributes() {
        List<TypedName<?>> names = new ArrayList<>();
        names.add(CommonAttributes.ENTITY_ID);
        if (columns != null) {
            columns.stream()
                   .filter(n -> n != CommonAttributes.ENTITY_ID)
                   .forEach(names::add);
        } else if (labeledColumns != null) {
            labeledColumns.values()
                          .stream()
                          .filter(n -> n != CommonAttributes.ENTITY_ID)
                          .forEach(names::add);
        }
        return AttributeSet.create(names);
    }

    /**
     * Instantiate a new entity builder.
     * @return A new entity builder.
     */
    public EntityBuilder newEntityBuilder() {
        if (builderFactory == null) {
            builderFactory = InstanceFactory.fromConstructor(entityBuilder, entityType);
        }
        return builderFactory.newInstance();
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
    public String toString() {
        return new ToStringBuilder(this)
                .append("delim", delimiter)
                .append("header", headerLines)
                .append("readHeader", readHeader)
                .append("entityType", entityType)
                .append("entityBuilder", entityBuilder)
                .append("columns", columns != null ? columns.size() : labeledColumns.size())
                .toString();
    }

    @Override
    public ObjectNode toJSON() {
        JsonNodeFactory nf = JsonNodeFactory.instance;

        ObjectNode json = nf.objectNode();
        json.put("format", "delimited");
        json.put("delimiter", delimiter);
        json.put("entity_type", entityType.getName());
        json.put("base_id", getBaseId());
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
                colObj.put("type", TypeUtils.makeTypeName(col.getType()));
            }
        } else if (labeledColumns != null) {
            ObjectNode cols = json.putObject("columns");
            for (Map.Entry<String,TypedName<?>> colE: labeledColumns.entrySet()) {
                ObjectNode colNode = cols.putObject(colE.getKey());
                colNode.put("name", colE.getValue().getName());
                colNode.put("type", TypeUtils.makeTypeName(colE.getValue().getType()));
            }
        } else {
            throw new IllegalStateException("no labels specified");
        }

        return json;
    }

    public static DelimitedColumnEntityFormat fromJSON(String name, ClassLoader loader, JsonNode json) {
        String fmt = json.path("format").asText("delimited").toLowerCase();
        String delim;
        switch (fmt) {
            case "csv":
                delim = ",";
                break;
            case "tsv":
            case "delimited":
                delim = "\t";
                break;
            default:
                throw new IllegalArgumentException("unsupported data format " + fmt);
        }
        JsonNode delimNode = json.path("delimiter");
        if (delimNode.isValueNode()) {
            delim = delimNode.asText();
        }

        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(delim);
        logger.debug("{}: using delimiter {}", name, delim);
        JsonNode header = json.path("header");
        boolean canUseColumnMap = false;
        if (header.isBoolean() && header.asBoolean()) {
            logger.debug("{}: reading header", name);
            format.setHeader(true);
            canUseColumnMap = true;
        } else if (header.isNumber()) {
            format.setHeaderLines(header.asInt());
            logger.debug("{}: skipping {} header lines", name, format.getHeaderLines());
        }
        format.setBaseId(json.path("base_id").asLong(0));

        String eTypeName = json.path("entity_type").asText("rating").toLowerCase();
        EntityType etype = EntityType.forName(eTypeName);
        logger.debug("{}: reading entities of type {}", name, etype);
        EntityDefaults entityDefaults = EntityDefaults.lookup(etype);
        format.setEntityType(etype);
        format.setEntityBuilder(entityDefaults != null ? entityDefaults.getDefaultBuilder() : BasicEntityBuilder.class);

        JsonNode columns = json.path("columns");
        if (columns.isMissingNode() || columns.isNull()) {
            List<TypedName<?>> defColumns = entityDefaults != null ? entityDefaults.getDefaultColumns() : null;
            if (defColumns == null) {
                throw new IllegalArgumentException("no columns specified and no default columns available");
            }

            for (TypedName<?> attr: entityDefaults.getDefaultColumns()) {
                format.addColumn(attr);
            }
        } else if (columns.isObject()) {
            if (!canUseColumnMap) {
                throw new IllegalArgumentException("cannot use column map without file header");
            }
            Iterator<Map.Entry<String, JsonNode>> colIter = columns.fields();
            while (colIter.hasNext()) {
                Map.Entry<String, JsonNode> col = colIter.next();
                format.addColumn(col.getKey(), parseAttribute(entityDefaults, col.getValue()));
            }
        } else if (columns.isArray()) {
            for (JsonNode col: columns) {
                format.addColumn(parseAttribute(entityDefaults, col));
            }
        } else {
            throw new IllegalArgumentException("invalid format for columns");
        }

        Class<? extends EntityBuilder> eb = TextEntitySource.parseEntityBuilder(loader, json);
        if (eb != null) {
            format.setEntityBuilder(eb);
        }
        logger.debug("{}: using entity builder {}", name, format.getEntityBuilder());

        return format;
    }

    @Override
    public LineEntityParser makeParser(List<String> header) {
        assert header.size() == getHeaderLines();

        if (usesHeader() && labeledColumns != null) {
            assert header.size() == 1;
            List<TypedName<?>> cols = new ArrayList<>();
            StringTokenizer tok = new StringTokenizer(header.get(0), delimiter);
            tok.setQuoteChar('"');
            while (tok.hasNext()) {
                String label = tok.next();
                cols.add(labeledColumns.get(label));
            }
            return new OrderedParser(cols, tok);
        } else {
            Preconditions.checkState(columns != null, "no columns specified");
            StringTokenizer tok = new StringTokenizer("", delimiter);
            tok.setQuoteChar('"');
            return new OrderedParser(columns, tok);
        }
    }

    private class OrderedParser extends LineEntityParser {
        int lineNo = 0;
        StringTokenizer tokenizer;
        List<TypedName<?>> fileColumns;

        public OrderedParser(List<TypedName<?>> columns, StringTokenizer tok) {
            fileColumns = columns;
            tokenizer = tok;
        }

        @Override
        public Entity parse(String line) {
            tokenizer.reset(line);
            lineNo += 1;

            EntityBuilder builder = newEntityBuilder()
                    .setId(lineNo + baseId);

            // since ID is already set, a subsequent ID column will properly override

            for (TypedName column: fileColumns) {
                String value = tokenizer.nextToken();
                if (value != null && column != null) {
                    Object parsed;
                    try {
                         parsed = column.parseString(value);
                    } catch (IllegalArgumentException e) {
                        throw new DataAccessException("line " + lineNo + ": error parsing column " + column, e);
                    }
                    builder.setAttribute(column, parsed);
                }
            }

            return builder.build();
        }
    }
}

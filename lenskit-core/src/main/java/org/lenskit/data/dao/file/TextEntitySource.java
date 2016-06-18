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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.lang3.ClassUtils;
import org.lenskit.data.entities.*;
import org.lenskit.util.io.LineStream;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Entity reader that loads entities from text data, often stored in a file.
 */
public class TextEntitySource implements EntitySource {
    private static final Logger logger = LoggerFactory.getLogger(TextEntitySource.class);
    private final String name;
    private CharSource source;
    private URL sourceURL;
    private EntityFormat format;

    /**
     * Construct a new text entity source.
     */
    public TextEntitySource() {
        this("<unnamed>");
    }

    /**
     * Construct a new text entity source.
     * @param name The source's name.
     */
    public TextEntitySource(String name) {
        this.name = name;
    }

    /**
     * Get the name of this data source.
     * @return The data source name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the source file for this reader.
     * @param file The source file.
     */
    public void setFile(Path file) {
        source = Files.asCharSource(file.toFile(), Charset.defaultCharset());
        try {
            sourceURL = file.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid URL " + file, e);
        }
    }

    /**
     * Set the URL of the input data.
     * @param url The URL of the input data.
     */
    public void setURI(URL url) {
        sourceURL = url;
        source = Resources.asCharSource(url, Charsets.UTF_8);
    }

    /**
     * Get the source file for this data source.
     * @return The source file.
     */
    public URL getURL() {
        return sourceURL;
    }

    /**
     * Set a string from which to read entities.
     */
    public void setSource(CharSequence text) {
        source = CharSource.wrap(text);
        sourceURL = null;
    }

    /**
     * Set the entity format for the reader.
     * @param format The entity format.
     */
    public void setFormat(EntityFormat format) {
        this.format = format;
    }

    /**
     * Get the entity format for the reader.
     * @return The entity format.
     */
    public EntityFormat getFormat() {
        return format;
    }

    /**
     * Open a stream to read entities from this source.
     * @return A stream of entities.
     */
    @Override
    public ObjectStream<Entity> openStream() throws IOException {
        BufferedReader reader = source.openBufferedStream();
        ObjectStream<String> lines = new LineStream(reader);
        int headerLines = format.getHeaderLines();
        List<String> header = new ArrayList<>();
        while (header.size() < headerLines) {
            String line = lines.readObject();
            if (line == null) {
                IOException ex = new IOException(String.format("expected %d header lines, found %d", headerLines, header.size()));
                try {
                    lines.close();
                } catch (Throwable th) {
                    ex.addSuppressed(th);
                }
                throw ex;
            }
            header.add(line);
        }
        LineEntityParser parser = format.makeParser(header);
        return ObjectStreams.transform(lines, parser);
    }

    /**
     * Create a file reader.
     * @param name The reader name.
     * @param object The configuring object.
     * @param base The base URI for source data.
     * @return The new entity reader.
     */
    static TextEntitySource fromJSON(String name, JsonNode object, URI base) {
        TextEntitySource source = new TextEntitySource(name);
        URI uri = base.resolve(object.get("file").asText());
        try {
            source.setURI(uri.toURL());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Cannot resolve URI " + uri, e);
        }
        logger.info("loading text file source {} to read from {}", name, source.getURL());

        String fmt = object.path("format").asText("delimited").toLowerCase();
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
        JsonNode delimNode = object.path("delimiter");
        if (delimNode.isValueNode()) {
            delim = delimNode.asText();
        }

        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setDelimiter(delim);
        logger.debug("{}: using delimiter {}", name, delim);
        JsonNode header = object.path("header");
        boolean canUseColumnMap = false;
        if (header.isBoolean() && header.asBoolean()) {
            logger.debug("{}: reading header", name);
            format.setHeader(true);
            canUseColumnMap = true;
        } else if (header.isNumber()) {
            format.setHeaderLines(header.asInt());
            logger.debug("{}: skipping {} header lines", format.getHeaderLines());
        }

        String eTypeName = object.path("entity_type").asText("rating").toLowerCase();
        EntityType etype = EntityType.forName(eTypeName);
        logger.debug("{}: reading entities of type {}", name, etype);
        EntityDefaults entityDefaults = EntityDefaults.lookup(etype);
        format.setEntityType(etype);
        format.setEntityBuilder(entityDefaults != null ? entityDefaults.getDefaultBuilder() : BasicEntityBuilder.class);

        JsonNode columns = object.path("columns");
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

        JsonNode ebNode = object.path("builder");
        if (ebNode.isTextual()) {
            String ebName = ebNode.asText();
            if (ebName.equals("basic")) {
                format.setEntityBuilder(BasicEntityBuilder.class);
            } else {
                Class bld;
                try {
                    // FIXME Use a configurable class loader
                    bld = ClassUtils.getClass(ebName);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("cannot load class " + ebName, e);
                }
                format.setEntityBuilder(bld);
            }
        }
        logger.debug("{}: using entity builder {}", format.getEntityBuilder());

        source.setFormat(format);
        return source;
    }

    private static TypedName<?> parseAttribute(EntityDefaults entityDefaults, JsonNode col) {
        if (col.isNull() || col.isMissingNode()) {
            return null;
        } else if (col.isObject()) {
            String name = col.get("name").asText();
            String type = col.get("type").asText();
            return TypedName.create(name, type);
        } else if (col.isTextual()) {
            TypedName<?> attr = entityDefaults.getAttributeDefaults(col.asText());
            if (attr == null) {
                attr = TypedName.create(col.asText(), col.asText().equals("id") ? Long.class : String.class);
            }
            return attr;
        } else {
            throw new IllegalArgumentException("invalid attribute specification: " + col.toString());
        }
    }
}

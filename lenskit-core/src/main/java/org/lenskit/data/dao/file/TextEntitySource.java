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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.grouplens.grapht.util.ClassLoaders;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.Describable;
import org.grouplens.lenskit.util.io.DescriptionWriter;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.lenskit.data.dao.DataAccessException;
import org.lenskit.data.entities.*;
import org.lenskit.util.io.LineStream;
import org.lenskit.util.io.ObjectStream;
import org.lenskit.util.io.ObjectStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Entity reader that loads entities from text data, often stored in a file.
 */
public class TextEntitySource implements EntitySource, Describable {
    private static final Logger logger = LoggerFactory.getLogger(TextEntitySource.class);
    private final String name;
    private CharSource source;
    private URL sourceURL;
    private EntityFormat format;
    private Map<String,Object> metadata = new HashMap<>();

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
    @Override
    public String getName() {
        return name;
    }

    @Nonnull
    @Override
    public Set<EntityType> getTypes() {
        return ImmutableSet.of(format.getEntityType());
    }

    /**
     * Set the source file for this reader.
     * @param file The source file.
     */
    public void setFile(Path file) {
        source = LKFileUtils.byteSource(file.toFile(), CompressionMode.AUTO)
                            .asCharSource(Charsets.UTF_8);
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
    public void setURL(URL url) {
        sourceURL = url;
        source = LKFileUtils.byteSource(url, CompressionMode.AUTO)
                            .asCharSource(Charsets.UTF_8);
    }

    /**
     * Get the input file path.
     * @return The input file path.
     */
    public Path getFile() {
        try {
            return Paths.get(sourceURL.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("invalid path URI " + sourceURL, e);
        }
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

    @Override
    public Map<String, Object> getMetadata() {
        return metadata;
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

    @Override
    public String toString() {
        ToStringBuilder tsb = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE);
        tsb.append("name", getName());
        tsb.append("url", getURL());
        tsb.append("format", getFormat());
        return tsb.build();
    }

    @Override
    public void describeTo(DescriptionWriter writer) {
        writer.putField("url", sourceURL);
        try {
            Path path = Paths.get(sourceURL.toURI());
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            writer.putField("size", attrs.size())
                  .putField("mtime", attrs.lastModifiedTime().toMillis());
        } catch (NoSuchFileException | FileNotFoundException e) {
            /* ok, file doesn't exist */
        } catch (FileSystemNotFoundException e) {
            /* ok, not a valid URL */
        } catch (IOException e) {
            throw new DataAccessException(e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("invalid URI", e);
        }

    }

    /**
     * Construct a JSON representation of this entity source, suitable for serialization to e.g. YAML.
     *
     * @param base The URI of the YAML file that will be generated, to generate relative URLs.
     * @return The JSON node.
     */
    public JsonNode toJSON(@Nullable URI base) {
        Path basePath = null;
        if (base != null) {
            try {
                basePath = Paths.get(base).getParent();
            } catch (FileSystemNotFoundException ex) {
                /* this is ok, just means we can't resolve the base URI */
            }
        }

        JsonNodeFactory nf = JsonNodeFactory.instance;

        ObjectNode object = nf.objectNode();
        object.put("type", "textfile");

        Path path = getFile();
        if (basePath != null) {
            path = basePath.relativize(path);
        }
        object.put("file", path.toString().replace(File.separatorChar, '/'));

        object.setAll(format.toJSON());

        return object;
    }

    /**
     * Create a file reader.
     * @param name The reader name.
     * @param object The configuring object.
     * @param base The base URI for source data.
     * @return The new entity reader.
     */
    static TextEntitySource fromJSON(String name, JsonNode object, URI base) {
        return fromJSON(name, object, base, ClassLoaders.inferDefault(TextEntitySource.class));
    }

    /**
     * Create a file reader.
     * @param name The reader name.
     * @param object The configuring object.
     * @param base The base URI for source data.
     * @return The new entity reader.
     */
    static TextEntitySource fromJSON(String name, JsonNode object, URI base, ClassLoader loader) {
        logger.debug("loading source {} with base URI {}", name, base);
        TextEntitySource source = new TextEntitySource(name);
        String filePath = object.path("file").asText(null);
        Preconditions.checkArgument(filePath != null, "no file path specified");
        URI uri = base.resolve(filePath);
        logger.debug("resolved file URI: {}", uri);

        try {
            source.setURL(uri.toURL());
        } catch (MalformedURLException e) {
            logger.error("cannot load from URI {}", uri);
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
                    bld = ClassUtils.getClass(loader, ebName);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("cannot load class " + ebName, e);
                }
                format.setEntityBuilder(bld);
            }
        }
        logger.debug("{}: using entity builder {}", format.getEntityBuilder());

        JsonNode metaNode = object.get("metadata");
        if (metaNode != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                source.metadata = mapper.readerFor(Map.class).readValue(metaNode);
            } catch (IOException e) {
                throw new IllegalArgumentException("cannnot process metadata", e);
            }
        }

        source.setFormat(format);
        return source;
    }

    private static TypedName<?> parseAttribute(EntityDefaults entityDefaults, JsonNode col) {
        if (col.isNull() || col.isMissingNode()) {
            return null;
        } else if (col.isObject()) {
            String name = col.path("name").asText(null);
            String type = col.path("type").asText(null);
            Preconditions.checkArgument(name != null, "no attribute name specified");
            Preconditions.checkArgument(type != null, "no attribute type specified");
            return TypedName.create(name, type);
        } else if (col.isTextual()) {
            String name = col.asText();
            TypedName<?> attr = entityDefaults != null ? entityDefaults.getAttributeDefaults(name) : null;
            if (attr == null) {
                attr = TypedName.create(col.asText(), col.asText().equals("id") ? (Class) Long.class : String.class);
            }
            return attr;
        } else {
            throw new IllegalArgumentException("invalid attribute specification: " + col.toString());
        }
    }
}

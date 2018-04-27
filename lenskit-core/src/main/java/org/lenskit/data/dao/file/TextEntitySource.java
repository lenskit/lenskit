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
import org.lenskit.util.io.CompressionMode;
import org.lenskit.util.describe.Describable;
import org.lenskit.util.describe.DescriptionWriter;
import org.lenskit.util.io.LKFileUtils;
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

    @Nullable
    @Override
    public Layout getLayout() {
        EntityType type = format.getEntityType();
        AttributeSet attrs = format.getAttributes();
        if (attrs != null) {
            return new Layout(type, attrs, format.getEntityBuilder());
        } else {
            return null;
        }
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
                IOException ex = new IOException(String.format("%s: expected %d header lines, found %d", sourceURL, headerLines, header.size()));
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
        EntityFormat format;
        switch (fmt) {
            case "csv":
            case "tsv":
            case "delimited":
                format = DelimitedColumnEntityFormat.fromJSON(name, loader, object);
                break;
            case "json":
                format = JSONEntityFormat.fromJSON(name, loader, object);
                break;
            default:
                throw new IllegalArgumentException("unknown entity format " + fmt);
        }

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

    static Class<? extends EntityBuilder> parseEntityBuilder(ClassLoader loader, JsonNode json) {
        JsonNode ebNode = json.path("builder");
        if (ebNode.isTextual()) {
            String ebName = ebNode.asText();
            if (ebName.equals("basic")) {
                return BasicEntityBuilder.class;
            } else {
                Class<?> bld;
                try {
                    bld = ClassUtils.getClass(loader, ebName);
                } catch (ClassNotFoundException e) {
                    throw new IllegalArgumentException("cannot load class " + ebName, e);
                }
                return bld.asSubclass(EntityBuilder.class);
            }
        } else if (ebNode.isMissingNode() || ebNode.isNull()) {
            return null;
        } else {
            throw new IllegalArgumentException("invalid entity builder: " + ebNode);
        }
    }

    static TypedName<?> parseAttribute(EntityDefaults entityDefaults, JsonNode col) {
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

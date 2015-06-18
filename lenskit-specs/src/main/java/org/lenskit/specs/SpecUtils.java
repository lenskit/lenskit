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
package org.lenskit.specs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility functions for working with specifications.
 */
public final class SpecUtils {
    private SpecUtils() {}

    static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("LenskitSpecs");
        mod.addSerializer(Path.class, new PathSerializer());
        mod.addDeserializer(Path.class, new PathDeserializer());
        mapper.registerModule(mod);
        return mapper;
    }

    /**
     * Read a specification type from a file.
     * @param type The specification type.
     * @param file The file to read from.
     * @param <T> The specification type.
     * @return A deserialized specification.
     * @throws IOException if there is an error reading the file.
     */
    public static <T> T load(Class<T> type, Path file) throws IOException {
        ObjectReader reader = createMapper().reader(type);
        try (Reader r = Files.newBufferedReader(file)) {
            return reader.readValue(r);
        }
    }

    /**
     * Parse a specification from a string.
     * @param type The specification type.
     * @param json A string of JSON data.
     * @param <T> The specification type.
     * @return A deserialized specification.
     */
    public static <T> T parse(Class<T> type, String json) {
        ObjectReader reader = createMapper().reader(type);
        try {
            return reader.readValue(json);
        } catch (IOException e) {
            throw new RuntimeException("error parsing JSON specification", e);
        }
    }

    /**
     * Convert a specification to a string.
     * @param spec The specification.
     * @return The JSON string representation of the specification.
     */
    public static String stringify(AbstractSpec spec) {
        ObjectWriter writer = createMapper().writer();
        try {
            return writer.writeValueAsString(spec);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error stringifying JSON", e);
        }
    }

    /**
     * Write a specification to a file.
     * @param spec The specification.
     * @param file The file to write to.
     * @throws IOException if there is an error writing the specification.
     */
    public static void write(AbstractSpec spec, Path file) throws IOException {
        ObjectWriter writer = createMapper().writer()
                                            .with(SerializationFeature.INDENT_OUTPUT);
        writer.writeValue(file.toFile(), spec);
    }
}

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
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Utility functions for working with specifications.
 */
public final class SpecUtils {
    private SpecUtils() {}

    public static ObjectMapper createMapper() {
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
        return reader.readValue(file.toFile());
    }

    /**
     * Read a list of specifications from a file.
     * @param type The specification type.
     * @param file The file to read from.
     * @param <T> The specification type.
     * @return A deserialized specification.
     * @throws IOException if there is an error reading the file.
     */
    public static <T> List<T> loadList(Class<T> type, Path file) throws IOException {
        ObjectMapper mapper = createMapper();
        JavaType listType = mapper.getTypeFactory().constructCollectionType(List.class, type);
        ObjectReader reader = createMapper().reader(listType);
        return reader.readValue(file.toFile());
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
    public static void write(Object spec, Path file) throws IOException {
        ObjectWriter writer = createMapper().writer()
                                            .with(SerializationFeature.INDENT_OUTPUT);
        writer.writeValue(file.toFile(), spec);
    }

    /**
     * Build an object from a specification.  On its own, this method does very little.  It depends on support from
     * objects in order to work.
     *
     * It operates as follows:
     *
     * 1.  Load the {@link SpecHandler}s using {@link java.util.ServiceLoader}.
     * 2.  Query each spec handler, in turn, to try to find one that can build an object of type `type` from the spec.
     * 3.  If no such handler is found, try to invoke a static `fromSpec(AbstractSpec)` method on `type`.
     * 4.  If no such method is found, or it returns `null`, throw {@link NoSpecHandlerFound}.
     *
     * @param type The type of object to build.
     * @param spec The specification to use.
     * @param <T> The built object type.
     * @return The built object.  Will be `null` if and only if `spec` is `null`.
     * @throws NoSpecHandlerFound if no spec handler or `fromSpec` method can be found.
     */
    @Nullable
    public static <T> T buildObject(@Nonnull Class<T> type, @Nullable AbstractSpec spec) {
        return buildObject(type, spec, null);
    }

    /**
     * Build an object from a specification.  On its own, this method does very little.  It depends on support from
     * objects in order to work.
     *
     * It operates as follows:
     *
     * 1.  Load the {@link SpecHandler}s using {@link java.util.ServiceLoader}.
     * 2.  Query each spec handler, in turn, to try to find one that can build an object of type `type` from the spec.
     * 3.  If no such handler is found, try to invoke a static `fromSpec(AbstractSpec)` method on `type`.
     * 4.  If no such method is found, or it returns `null`, throw {@link NoSpecHandlerFound}.
     *
     * @param type The type of object to build.
     * @param spec The specification to use.
     * @param cl The class loader to search.
     * @param <T> The built object type.
     * @return The built object.  Will be `null` if and only if `spec` is `null`.
     * @throws NoSpecHandlerFound if no spec handler or `fromSpec` method can be found.
     */
    @Nullable
    public static <T> T buildObject(@Nonnull Class<T> type, @Nullable AbstractSpec spec, ClassLoader cl) {
        if (spec == null) {
            return null;
        }

        ServiceLoader<SpecHandler> loader;
        if (cl == null) {
            loader = ServiceLoader.load(SpecHandler.class);
        } else {
            loader = ServiceLoader.load(SpecHandler.class, cl);
        }
        for (SpecHandler h: loader) {
            T obj = h.build(type, spec);
            if (obj != null) {
                return obj;
            }
        }

        try {
            T obj = type.cast(MethodUtils.invokeStaticMethod(type, "fromSpec", spec));
            if (obj == null) {
                throw new NoSpecHandlerFound("No spec handler to build " + type + " from " + spec);
            } else {
                return obj;
            }
        } catch (NoSuchMethodException e) {
            throw new NoSpecHandlerFound("No spec handler to build " + type + " from " + spec);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("access error invoking fromSpec on " + type, e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Error occurred in fromSpec on " + type, e);
        }
    }

    /**
     * Make a copy of a spec. Rather than implementing the complicated {@link Cloneable} infrastructure, we just
     * round-trip the spec through JSON and copy all specs easily.
     *
     * @param spec The spec to copy.
     * @param <T> The spec type.
     * @return The copied spec.
     */
    @SuppressWarnings("unchecked")
    public static <T extends AbstractSpec> T copySpec(T spec) {
        if (spec == null) {
            return null;
        }

        ObjectMapper mapper = createMapper();
        JsonNode node = mapper.convertValue(spec, JsonNode.class);
        return (T) mapper.convertValue(node, spec.getClass());
    }
}

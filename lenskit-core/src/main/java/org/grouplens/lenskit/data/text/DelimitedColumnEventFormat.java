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
package org.grouplens.lenskit.data.text;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.grapht.util.ClassLoaders;
import org.lenskit.data.events.Event;
import org.lenskit.data.events.EventBuilder;
import org.lenskit.data.events.EventTypeResolver;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read events from delimited columns (CSV, TSV, etc.).
 *
 * @since 2.2
 */
public final class DelimitedColumnEventFormat implements EventFormat, Serializable {
    private static final long serialVersionUID = 1L;

    private final Class<? extends EventBuilder> builderType;

    @Nonnull
    private String delimiter = "\t";
    @Nonnull
    private List<Field> fieldList;
    private int headerLines = 0;

    /**
     * Construct a new event format.
     * @param delim The delimiter.
     * @param bld The builder type.
     * @param fields The fields.
     */
    DelimitedColumnEventFormat(String delim, Class<? extends EventBuilder> bld, List<Field> fields) {
        delimiter = delim;
        builderType = bld;
        if (fields == null) {
            fieldList = Collections.emptyList();
        } else {
            setFields(fields);
        }
    }

    /**
     * Create a new delimited column format from a named type.
     * @param typeName The name of the type definition.
     * @return The column format.
     * @throws java.lang.IllegalArgumentException if the specified type cannot be found.
     */
    @Nonnull
    @SuppressWarnings("rawtypes")
    public static DelimitedColumnEventFormat create(String typeName) {
        return create(typeName, ClassLoaders.inferDefault(DelimitedColumnEventFormat.class));
    }

    /**
     * Create a new delimited column format from a named type.
     *
     * @param typeName The name of the type definition.
     * @param loader   The class loader to load from.
     * @return The column format.
     * @throws java.lang.IllegalArgumentException if the specified type cannot be found.
     */
    @Nonnull
    @SuppressWarnings("rawtypes")
    public static DelimitedColumnEventFormat create(String typeName, ClassLoader loader) {
        EventTypeResolver resolver = EventTypeResolver.create(loader);
        Class<? extends EventBuilder> type = resolver.getEventBuilder(typeName);
        if (type != null) {
            return create(type);
        } else {
            throw new IllegalArgumentException("invalid event type " + typeName);
        }
    }

    /**
     * Create an event format from an event builder class.
     * @param builder The builder class.
     * @return The event format.
     */
    public static DelimitedColumnEventFormat create(Class<? extends EventBuilder> builder) {
        DefaultFields dft = builder.getAnnotation(DefaultFields.class);
        List<Field> fields = new ArrayList<>();
        if (dft != null) {
            for (String name: dft.value()) {
                Field field = Fields.byName(builder, name);
                if (field == null) {
                    throw new IllegalArgumentException("class " + builder + " does not have field " + name);
                }
                fields.add(field);
            }
        }
        return new DelimitedColumnEventFormat("\t", builder, fields);
    }

    @Nonnull
    public String getDelimiter() {
        return delimiter;
    }

    public DelimitedColumnEventFormat setDelimiter(@Nonnull String delim) {
        delimiter = delim;
        return this;
    }

    @Override
    public int getHeaderLines() {
        return headerLines;
    }

    /**
     * Set the number of lines to skip before reading events.
     * @param lines The number of lines to skip.
     * @return The number of header lines to skip.
     */
    public DelimitedColumnEventFormat setHeaderLines(int lines) {
        headerLines = lines;
        return this;
    }

    /**
     * Set the fields to be parsed.
     *
     * @param fields The field names to be parsed by this format.
     * @return The format (for chaining).
     */
    public DelimitedColumnEventFormat setFields(@Nonnull String... fields) {
        return setFieldsByName(ImmutableList.copyOf(fields));
    }

    /**
     * Set fields by string names.
     * @param fields The fields.
     * @return The names.
     */
    public DelimitedColumnEventFormat setFieldsByName(@Nonnull List<String> fields) {
        List<Field> fieldObjs = new ArrayList<>();
        for (String fname: fields) {
            Field field = Fields.byName(builderType, fname);
            if (field == null) {
                throw new IllegalArgumentException("no field " + field);
            }
            fieldObjs.add(field);
        }
        return setFields(fieldObjs);
    }

    /**
     * Set the fields to be parsed.
     *
     * @param fields The fields to be parsed by this format.
     * @return The format (for chaining).
     */
    public DelimitedColumnEventFormat setFields(@Nonnull Field... fields) {
        return setFields(ImmutableList.copyOf(fields));
    }

    /**
     * Set the fields to be parsed.
     *
     * @param fields The fields to be parsed by this format.
     * @return The format (for chaining).
     */
    public DelimitedColumnEventFormat setFields(@Nonnull List<Field> fields) {
        boolean seenOptional = false;
        for (Field fld: fields) {
            if (fld == null) {
                throw new NullPointerException("field is null");
            }
            if (!fld.getBuilderType().isAssignableFrom(builderType)) {
                throw new IllegalArgumentException("Field " + fld + " cannot configure builder " + builderType);
            }
            if (seenOptional && !fld.isOptional()) {
                throw new IllegalArgumentException("Non-optional field {} after optional field {}");
            } else if (fld.isOptional()) {
                seenOptional = true;
            }
        }
        fieldList = ImmutableList.copyOf(fields);
        return this;
    }

    /**
     * Get the field list.
     * @return The list of fields.
     */
    public List<Field> getFields() {
        return fieldList;
    }

    private Event parse(StrTokenizer tok, EventBuilder<?> builder) throws InvalidRowException {
        for (Field field: fieldList) {
            String token = tok.nextToken();
            if (token == null && !field.isOptional()) {
                throw new InvalidRowException("Non-optional field " + field.toString() + " missing");
            }
            field.apply(token, builder);
        }
        return builder.build();
    }

    @Override
    public Event parse(String line) throws InvalidRowException {
        StrTokenizer tok = new StrTokenizer(line, delimiter);
        return parse(tok, newBuilder());
    }

    @Override
    public Object newContext() {
        return new Context();
    }

    @Override
    public Event parse(String line, Object context) throws InvalidRowException {
        Context ctx = (Context) context;
        ctx.builder.reset();
        ctx.tokenizer.reset(line);
        return parse(ctx.tokenizer, ctx.builder);
    }

    private EventBuilder newBuilder() {
        try {
            return builderType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("cannot instantiate " + builderType, e);
        }
    }

    public Class<? extends EventBuilder> getBuilderType() {
        return builderType;
    }

    private class Context {
        public final StrTokenizer tokenizer = new StrTokenizer().setDelimiterString(delimiter);
        public final EventBuilder<?> builder = newBuilder();
    }
}

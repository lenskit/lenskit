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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.text.StrTokenizer;
import org.grouplens.grapht.util.ClassLoaders;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.EventBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Read events from delimited columns (CSV, TSV, etc.).
 *
 * @since 2.2
 */
public final class DelimitedColumnEventFormat implements EventFormat {
    @Nonnull
    private final EventTypeDefinition eventTypeDef;

    @Nonnull
    private String delimiter = "\t";
    @Nonnull
    private List<Field> fieldList;

    @Inject
    public DelimitedColumnEventFormat(@Nonnull EventTypeDefinition etd) {
        eventTypeDef = etd;
        setFields(etd.getDefaultFields());
    }

    /**
     * Create a new delimited column format.
     * @param etd The event type definition.
     * @return The column format.
     */
    @Nonnull
    public static DelimitedColumnEventFormat create(@Nonnull EventTypeDefinition etd) {
        Preconditions.checkNotNull(etd, "type definition");
        return new DelimitedColumnEventFormat(etd);
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
        ServiceLoader<EventTypeDefinition> sl = ServiceLoader.load(EventTypeDefinition.class, loader);
        EventTypeDefinition typedef = null;
        for (EventTypeDefinition etd : sl) {
            if (etd.getName().equalsIgnoreCase(typeName)) {
                if (typedef == null) {
                    typedef = etd;
                } else {
                    throw new RuntimeException("Multiple type definitions found for " + typeName);
                }
            }
        }
        if (typedef == null) {
            throw new IllegalArgumentException("Invalid event type " + typeName);
        } else {
            return create(typedef);
        }
    }

    @Nonnull
    public EventTypeDefinition getEventTypeDefinition() {
        return eventTypeDef;
    }

    @Nonnull
    public String getDelimiter() {
        return delimiter;
    }

    public DelimitedColumnEventFormat setDelimiter(@Nonnull String delim) {
        delimiter = delim;
        return this;
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
        if (!fields.containsAll(eventTypeDef.getRequiredFields())) {
            throw new IllegalArgumentException("missing fields");
        }

        Predicate<Class<? extends EventBuilder>> canConfigBuilderType = new Predicate<Class<? extends EventBuilder>>() {
            @Override
            public boolean apply(@Nullable Class<? extends EventBuilder> input) {
                return input != null && input.isAssignableFrom(eventTypeDef.getBuilderType());
            }
        };
        for (Field fld: fields) {
            if (!Iterables.any(fld.getExpectedBuilderTypes(), canConfigBuilderType)) {
                throw new IllegalArgumentException("Field " + fld + " cannot configure builder " + eventTypeDef.getBuilderType());
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
            if (token == null && field != null && !field.isOptional()) {
                // fixme make nulls optional if they are at the end
                throw new InvalidRowException("Non-optional field " + field.toString() + " missing");
            }
            if (field != null) {
                field.apply(token, builder);
            }
        }
        return builder.build();
    }

    @Override
    public Event parse(String line) throws InvalidRowException {
        StrTokenizer tok = new StrTokenizer(line, delimiter);
        return parse(tok, eventTypeDef.newBuilder());
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

    private class Context {
        public final StrTokenizer tokenizer = new StrTokenizer();
        public final EventBuilder<?> builder = eventTypeDef.newBuilder();
    }
}

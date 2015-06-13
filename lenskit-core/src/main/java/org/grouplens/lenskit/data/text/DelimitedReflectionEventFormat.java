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

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.text.StrTokenizer;
import org.lenskit.data.events.Event;
import org.lenskit.data.events.EventBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Read events from delimited columns (CSV, TSV, etc.).
 *
 * @since 2.2
 */
public final class DelimitedReflectionEventFormat implements EventFormat {
    private final static Map<Class,Function<String,Object>> CONVERTERS;

    static {
        ImmutableMap.Builder<Class,Function<String,Object>> bld = ImmutableMap.builder();
        bld.put(Long.class, new Function<String,Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable String input) {
                return Long.parseLong(input);
            }
        });
        bld.put(Double.class, new Function<String,Object>() {
            @Nullable
            @Override
            public Object apply(@Nullable String input) {
                return Double.parseDouble(input);
            }
        });
        bld.put(String.class, (Function) Functions.identity());
        CONVERTERS = bld.build();
    }

    private final List<Field> fields;
    private final Class<? extends EventBuilder<?>> builderType;

    @Nonnull
    private String delimiter = "\t";
    private int headerLines = 0;

    /**
     * Construct a new event format.
     * @param delim The delimiter.
     */
    public DelimitedReflectionEventFormat(@ColumnSeparator String delim, Class<? extends EventBuilder<?>> btype, List<Field> fields) {
        delimiter = delim;
        this.fields = fields;
        builderType = btype;
    }

    /**
     * Create a new delimited column format.
     * @return The column format.
     */
    @Nonnull
    public static DelimitedReflectionEventFormat create(String delim, final Class<? extends EventBuilder<?>> type, String... fields) {
        ImmutableList.Builder<Field> theFields = ImmutableList.builder();
        for (String f: fields) {
            String setterName = "set" + Character.toUpperCase(f.charAt(0)) + f.substring(1);
            Method setter = null;
            for (Method m: type.getMethods()) {
                if (m.getName().equals(setterName) && !m.isBridge()) {
                    System.out.println(m);
                    if (setter == null) {
                        setter = m;
                    } else {
                        throw new IllegalArgumentException("Multiple methods named " + setterName);
                    }
                }
            }
            if (setter == null) {
                throw new IllegalArgumentException("No method " + setterName + " found");
            }
            Class<?>[] atypes = setter.getParameterTypes();
            if (atypes.length != 1) {
                throw new IllegalArgumentException("Method " + setterName + " takes too many arguments");
            }
            final Method theSetter = setter;
            Class<?> atype = atypes[0];
            if (atype.isPrimitive()) {
                atype = ClassUtils.primitiveToWrapper(atype);
            }
            final Function<String,Object> convert = CONVERTERS.get(atype);
            if (convert == null) {
                throw new IllegalArgumentException("Field type " + atypes[0] + " not allowed.");
            }
            theFields.add(new Field() {
                @Override
                public boolean isOptional() {
                    return false;
                }

                @Override
                public void apply(String token, EventBuilder builder) {
                    try {
                        theSetter.invoke(builder, convert.apply(token));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public Set<Class<? extends EventBuilder>> getExpectedBuilderTypes() {
                    return (Set) Collections.singleton(type);
                }
            });
        }

        return new DelimitedReflectionEventFormat(delim, type, theFields.build());
    }

    @Nonnull
    public String getDelimiter() {
        return delimiter;
    }

    public DelimitedReflectionEventFormat setDelimiter(@Nonnull String delim) {
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
    public DelimitedReflectionEventFormat setHeaderLines(int lines) {
        headerLines = lines;
        return this;
    }

    private Event parse(StrTokenizer tok, EventBuilder<?> builder) throws InvalidRowException {
        builder.reset();
        for (Field field: fields) {
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
        Context ctx = new Context();
        ctx.tokenizer.reset(line);
        return parse(line, ctx);
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
        public final StrTokenizer tokenizer = new StrTokenizer().setDelimiterString(delimiter);
        public final EventBuilder<?> builder;

        private Context() {
            try {
                builder = builderType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

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

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.lenskit.data.events.EventBuilder;
import org.lenskit.data.events.LikeBatchBuilder;
import org.lenskit.data.ratings.RatingBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("rawtypes")
public final class Fields {
    private Fields() {}

    /**
     * The user ID field.
     * @return A field definition for the user ID.
     */
    public static Field user() {
        return CommonFields.USER;
    }

    /**
     * The item ID field.
     * @return A field definition for the item ID.
     */
    public static Field item() {
        return CommonFields.ITEM;
    }

    /**
     * A required timestamp field.
     * @return A field definition for a required timestamp field.
     */
    public static Field timestamp() {
        return timestamp(true);
    }

    /**
     * Get a common field by name.  Common fields are 'user', 'item', and 'timestamp'.
     * @return The field, or `null` if `name` is an unknown field.
     */
    public static Field commonField(String name) {
        if (name == null) {
            return ignored();
        }
        switch (name) {
        case "user":
            return user();
        case "item":
            return item();
        case "timestamp":
            return timestamp();
        case "timestamp?":
            return timestamp(false);
        default:
            return null;
        }
    }

    /**
     * Get a field by name.  It first looks up the common fields, and if none of them apply, creates
     * a reflection-based field.
     *
     * @param eb The event builder.
     * @param name The field name.  The name can be suffixed with "?" to make it optional.
     * @return The field, or `null` if no such field can be defined.
     */
    public static Field byName(Class<? extends EventBuilder> eb, String name) {
        Field field = commonField(name);
        if (field != null) {
            return field;
        }

        boolean optional = false;
        if (name.endsWith("?")) {
            optional = true;
            name = name.substring(0, name.length() - 1);
        }

        String setterName = "set" + StringUtils.capitalize(name);
        Method setter = null;
        Method annotated = null;
        for (Method m: eb.getMethods()) {
            FieldName nameAnnot = m.getAnnotation(FieldName.class);
            if (nameAnnot != null) {
                if (nameAnnot.value().equals(name)) {
                    annotated = m;
                }
            } else if (m.getName().equals(setterName) && !m.isBridge()) {
                if (setter == null) {
                    setter = m;
                } else {
                    throw new IllegalArgumentException("Multiple methods named " + setterName);
                }
            }
        }
        if (annotated != null) {
            setter = annotated;
        }

        if (setter == null) {
            throw new IllegalArgumentException("No method found for field " + name);
        }
        Class<?>[] atypes = setter.getParameterTypes();
        if (atypes.length != 1) {
            throw new IllegalArgumentException("Method " + setter.getName() + " takes too many arguments");
        }
        final Method theSetter = setter;
        Class<?> atype = atypes[0];
        if (atype.isPrimitive()) {
            atype = ClassUtils.primitiveToWrapper(atype);
        }
        StringConverter<Object> convert = StringConvert.INSTANCE.findConverterNoGenerics(atype);
        if (convert == null) {
            throw new IllegalArgumentException("Field type " + atypes[0] + " not allowed.");
        }
        return new ReflectionField(name, theSetter, eb, atype, convert, optional);
    }

    /**
     * A field that is ignored.
     * @return A field definition for a field to ignore.
     */
    public static Field ignored() {
        return CommonFields.IGNORED;
    }

    /**
     * A field that is ignored and may be optional.
     * @param optional {@code true} if the field is optional (it may or may not appear).
     * @return A field definition for a field to ignore.
     */
    public static Field ignored(boolean optional) {
        if (optional) {
            return CommonFields.OPTIONAL_IGNORE;
        } else {
            return CommonFields.IGNORED;
        }
    }

    public static Field rating() {
        return ValueFields.RATING;
    }

    /**
     * Create a list of fields.  This helper is structured to aid in type inference.
     *
     * @param fields The fields to put in the list.
     * @return The field list.
     */
    public static List<Field> list(Field... fields) {
        return ImmutableList.copyOf(fields);
    }

    /**
     * A timestamp field.
     * @param required {@code true} if the timestamp is required, {@code false} if it is optional.
     * @return A field definition for a required timestamp field.
     */
    public static Field timestamp(boolean required) {
        if (required) {
            return CommonFields.TIMESTAMP;
        } else {
            return CommonFields.OPTIONAL_TIMESTAMP;
        }
    }

    /**
     * A plus count field.
     * @return A field definition for a required plus count field.
     */
    public static Field likeCount() {
        return ValueFields.LIKE_COUNT;
    }

    private static enum CommonFields implements Field {
        IGNORED(null) {
            @Override
            public void apply(String token, EventBuilder builder) {
                /* do nothing */
            }
        },
        OPTIONAL_IGNORE(null) {
            @Override
            public boolean isOptional() {
                return true;
            }

            @Override
            public void apply(String token, EventBuilder builder) {
                /* do nothing */
            }
        },
        USER("user") {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setUserId(Long.parseLong(token));
            }
        },

        ITEM("item") {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setItemId(Long.parseLong(token));
            }
        },
        TIMESTAMP("timestamp") {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setTimestamp(Long.parseLong(token));
            }
        },
        OPTIONAL_TIMESTAMP("timestamp?") {
            @Override
            public void apply(String token, EventBuilder builder) {
                if (token == null) {
                    builder.setTimestamp(-1);
                } else {
                    builder.setTimestamp(Long.parseLong(token));
                }
            }

            @Override
            public boolean isOptional() {
                return true;
            }
        };

        private final String name;

        CommonFields(String name) {
            this.name = name;
        }

        @Override
        public Class<? extends EventBuilder> getBuilderType() {
            return EventBuilder.class;
        }

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static enum ValueFields implements Field {
        RATING(RatingBuilder.class, "rating") {
            @Override
            public void apply(String token, EventBuilder builder) {
                RatingBuilder rb = (RatingBuilder) builder;
                if (token == null || token.equals("")) {
                    rb.clearRating();
                } else {
                    double v = Double.parseDouble(token);
                    if (Double.isNaN(v)) {
                        rb.clearRating();
                    } else {
                        rb.setRating(v);
                    }
                }
            }
        },

        LIKE_COUNT(LikeBatchBuilder.class, "count") {
            @Override
            public void apply(String token, EventBuilder builder) {
                ((LikeBatchBuilder) builder).setCount(Integer.parseInt(token));
            }
        };

        private final Class<? extends EventBuilder> builderType;
        private final String name;

        ValueFields(Class<? extends EventBuilder> bt, String name) {
            builderType = bt;
            this.name = name;
        }

        @Override
        public Class<? extends EventBuilder> getBuilderType() {
            return builderType;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private static class ReflectionField implements Field {
        private final String fieldName;
        private final Method setter;
        private final Class<? extends EventBuilder> builderType;
        private final Class<?> argType;
        private final StringConverter<Object> converter;
        private final boolean optional;

        public ReflectionField(String name, Method theSetter, Class<? extends EventBuilder> btype, Class<?> atype, StringConverter<Object> convert, boolean optional) {
            fieldName = name;
            this.setter = theSetter;
            builderType = btype;
            this.argType = atype;
            this.converter = convert;
            this.optional = optional;
        }

        @Override
        public Class<? extends EventBuilder> getBuilderType() {
            return builderType;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public String getName() {
            return fieldName;
        }

        @Override
        public void apply(String token, EventBuilder builder) {
            if (token == null ) {
                if (!optional) {
                    throw new IllegalArgumentException("null string provided for optional value");
                }
            } else {
                try {
                    setter.invoke(builder, converter.convertFromString(argType, token));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

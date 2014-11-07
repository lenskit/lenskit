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
import org.grouplens.lenskit.data.event.EventBuilder;
import org.grouplens.lenskit.data.event.LikeBatchBuilder;
import org.grouplens.lenskit.data.event.LikeBuilder;
import org.grouplens.lenskit.data.event.RatingBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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
     * The rating field.
     * @return A field definition for a rating field.
     */
    public static Field rating() {
        return ValueFields.RATING;
    }

    /**
     * A field that is ignored.
     * @return A field definition for a field to ignore.
     */
    public static Field ignored() {
        return CommonFields.IGNORED;
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
        IGNORED {
            @Override
            public void apply(String token, EventBuilder builder) {
                /* do nothing */
            }
        },
        USER {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setUserId(Long.parseLong(token));
            }
        },

        ITEM {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setItemId(Long.parseLong(token));
            }
        },
        TIMESTAMP {
            @Override
            public void apply(String token, EventBuilder builder) {
                builder.setTimestamp(Long.parseLong(token));
            }
        },
        OPTIONAL_TIMESTAMP {
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

        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public Set<Class<? extends EventBuilder>> getExpectedBuilderTypes() {
            return Collections.<Class<? extends EventBuilder>>singleton(EventBuilder.class);
        }

    }

    private static enum ValueFields implements Field {
        RATING {
            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public Set<Class<? extends EventBuilder>> getExpectedBuilderTypes() {
                return Collections.<Class<? extends EventBuilder>>singleton(RatingBuilder.class);
            }

            @Override
            public void apply(String token, EventBuilder builder) {
                // TODO Add support for unrate events
                ((RatingBuilder) builder).setRating(Double.parseDouble(token));
            }
        },

        LIKE_COUNT {
            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public Set<Class<? extends EventBuilder>> getExpectedBuilderTypes() {
                return Collections.<Class<? extends EventBuilder>>singleton(LikeBuilder.class);
            }

            @Override
            public void apply(String token, EventBuilder builder) {
                ((LikeBatchBuilder) builder).setCount(Integer.parseInt(token));
            }
        }
    }
}

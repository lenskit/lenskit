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

import org.grouplens.lenskit.data.event.EventBuilder;
import org.grouplens.lenskit.data.event.RatingBuilder;

public final class Fields {
    private Fields() {}

    /**
     * The user ID field.
     * @return A field definition for the user ID.
     */
    public static Field<EventBuilder> user() {
        return CommonFields.USER;
    }

    /**
     * The item ID field.
     * @return A field definition for the item ID.
     */
    public static Field<EventBuilder> item() {
        return CommonFields.ITEM;
    }


    /**
     * A required timestamp field.
     * @return A field definition for a required timestamp field.
     */
    public static Field<EventBuilder> timestamp() {
        return timestamp(true);
    }

    /**
     * The rating field.
     * @return A field definition for a rating field.
     */
    public static Field<RatingBuilder> rating() {
        return RatingFields.RATING;
    }

    /**
     * A timestamp field.
     * @param required {@code true} if the timestamp is required, {@code false} if it is optional.
     * @return A field definition for a required timestamp field.
     */
    public static Field<EventBuilder> timestamp(boolean required) {
        if (required) {
            return TimestampFieldDefs.REQUIRED;
        } else {
            return TimestampFieldDefs.OPTIONAL;
        }
    }

    private static enum CommonFields implements Field<EventBuilder> {
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
        };

        @Override
        public boolean isOptional() {
            return false;
        }
    }

    private static enum TimestampFieldDefs implements Field<EventBuilder> {
        REQUIRED {
            @Override
            public boolean isOptional() {
                return false;
            }
        },
        OPTIONAL {
            @Override
            public boolean isOptional() {
                return true;
            }
        };

        @Override
        public void apply(String token, EventBuilder builder) {
            if (token == null) {
                builder.setTimestamp(-1);
            } else {
                builder.setTimestamp(Long.parseLong(token));
            }
        }
    }

    private static enum RatingFields implements Field<RatingBuilder> {
        RATING {
            @Override
            public boolean isOptional() {
                return false;
            }

            @Override
            public void apply(String token, RatingBuilder builder) {
                // TODO Add support for unrate events
                builder.setRating(Double.parseDouble(token));
            }
        }
    }
}

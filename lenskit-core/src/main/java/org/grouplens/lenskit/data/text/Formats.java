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

/**
 * Utility classes for event formats.
 *
 * @since 2.2
 */
public final class Formats {
    private Formats() {}

    /**
     * A basic format of ratings in a CSV file.  The expected format is (<em>user</em>, <em>item</em>,
     * <em>rating</em>, <em>timestamp</em>), where the timestamp is optional.
     * @param delim The delimiter.
     * @return An event format for reading ratings from a CSV file.
     */
    public static DelimitedColumnEventFormat delimitedRatings(String delim) {
        DelimitedColumnEventFormat fmt = new DelimitedColumnEventFormat(new RatingEventType());
        fmt.setDelimiter(delim);
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp(false));
        return fmt;
    }

    /**
     * A basic format of ratings in a CSV file.  The expected format is (<em>user</em>, <em>item</em>,
     * <em>rating</em>, <em>timestamp</em>), where the timestamp is optional.
     * @return An event format for reading ratings from a CSV file.
     */
    public static DelimitedColumnEventFormat csvRatings() {
        return delimitedRatings(",");
    }

    /**
     * Get a format for reading the ML-100K data set.
     *
     * @return A format for using {@link TextEventDAO} to read the ML-100K data set.
     */
    public static DelimitedColumnEventFormat ml100kFormat() {
        DelimitedColumnEventFormat fmt = new DelimitedColumnEventFormat(new RatingEventType());
        fmt.setDelimiter("\t");
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp());
        return fmt;
    }

    /**
     * Get a format for reading the MovieLens data sets (other than 100K).
     *
     * @return A format for using {@link TextEventDAO} to read the ML-1M and ML-10M data sets.
     */
    public static DelimitedColumnEventFormat movieLensFormat() {
        DelimitedColumnEventFormat fmt = new DelimitedColumnEventFormat(new RatingEventType());
        fmt.setDelimiter("::");
        fmt.setFields(Fields.user(), Fields.item(), Fields.rating(), Fields.timestamp());
        return fmt;
    }
}

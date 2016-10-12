/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2016 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.data.dao.file;

import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.entities.CommonTypes;
import org.lenskit.data.ratings.RatingBuilder;

/**
 * Utility methods relating to entity formats.
 */
public final class Formats {
    /**
     * Read a CSV file of ratings without a header.  The ratings are assumed to be in `user, item, rating[, timestamp]`
     * format.
     *
     * @return An event format reading ratings without a header.
     */
    public static DelimitedColumnEntityFormat csvRatings() {
        return delimitedRatings(",");
    }

    /**
     * Read a TSV file of ratings without a header.  The ratings are assumed to be in `user, item, rating[, timestamp]`
     * format.
     *
     * @return An event format reading ratings without a header.
     */
    public static DelimitedColumnEntityFormat tsvRatings() {
        return delimitedRatings("\t");
    }

    /**
     * Read a delimited text file of ratings without a header.  The ratings are assumed to be in `user, item, rating[, timestamp]`
     * format.
     *
     * @param delim The delimiter.
     * @return An event format reading ratings without a header.
     */
    public static DelimitedColumnEntityFormat delimitedRatings(String delim) {
        DelimitedColumnEntityFormat format = new DelimitedColumnEntityFormat();
        format.setEntityType(CommonTypes.RATING);
        format.setEntityBuilder(RatingBuilder.class);
        format.addColumns(CommonAttributes.USER_ID,
                          CommonAttributes.ITEM_ID,
                          CommonAttributes.RATING,
                          CommonAttributes.TIMESTAMP);
        format.setDelimiter(delim);
        return format;
    }
}

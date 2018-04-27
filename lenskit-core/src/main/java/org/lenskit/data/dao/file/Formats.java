/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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

/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.lenskit.data.dao;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.data.event.AbstractEventCursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.util.DelimitedTextCursor;
import org.picocontainer.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.io.BufferedReader;

public class DelimitedTextRatingCursor extends AbstractEventCursor<Rating> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private final String fileName;
    private MutableRating rating;
    private DelimitedTextCursor rowCursor;

    public DelimitedTextRatingCursor(@WillCloseWhenClosed @Nonnull BufferedReader s) {
        this(s, null, System.getProperty("lenskit.delimiter", "\t"));
    }

    public DelimitedTextRatingCursor(@WillCloseWhenClosed @Nonnull BufferedReader s,
                                     @Nullable String name,
                                     @Nonnull String delimiter) {
        fileName = name;
        rating = new MutableRating();
        rowCursor = new DelimitedTextCursor(s, delimiter);
    }

    @Override
    public void close() {
        rowCursor.close();
        rating = null;
    }

    @Override
    public Rating poll() {
        Preconditions.checkState(rating != null, "cursor is closed");
        while (rowCursor.hasNext()) {
            String[] fields = rowCursor.next();
            if (fields.length < 3) {
                logger.error("{}:{}: invalid input, skipping line",
                             fileName, rowCursor.getLineNumber());
                continue;
            }

            rating.setId(rowCursor.getLineNumber());
            rating.setUserId(Long.parseLong(fields[0]));
            rating.setItemId(Long.parseLong(fields[1]));
            rating.setRating(Double.parseDouble(fields[2]));
            rating.setTimestamp(-1);
            if (fields.length >= 4)
                rating.setTimestamp(Long.parseLong(fields[3]));

            return rating;
        }

        return null;
    }
}

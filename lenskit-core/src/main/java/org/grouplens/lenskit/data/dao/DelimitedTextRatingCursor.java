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
package org.grouplens.lenskit.data.dao;

import com.google.common.base.Preconditions;
import org.grouplens.lenskit.cursors.AbstractPollingCursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;
import org.grouplens.lenskit.util.DelimitedTextCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.WillCloseWhenClosed;
import java.io.BufferedReader;

/**
 * Cursor that parses arbitrary delimited text.
 *
 * @compat Public
 * @deprecated Deprecated alias for {@link org.grouplens.lenskit.data.text.DelimitedTextRatingCursor}.
 */
@Deprecated
public class DelimitedTextRatingCursor extends org.grouplens.lenskit.data.text.DelimitedTextRatingCursor {
    /**
     * Construct a rating cursor from a reader.
     *
     * @param s         The reader to read.
     * @param name      The file name (for error messages).
     * @param delimiter The delimiter.
     */
    public DelimitedTextRatingCursor(@WillCloseWhenClosed @Nonnull BufferedReader s,
                                     @Nullable String name,
                                     @Nonnull String delimiter) {
        super(s, name, delimiter);
    }
}

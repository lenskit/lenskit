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

import org.grouplens.lenskit.util.io.CompressionMode;

import javax.inject.Inject;
import java.io.File;

/**
 * Deprecated alias for {@link org.grouplens.lenskit.data.text.SimpleFileRatingDAO}.
 * @deprecated Use {@link org.grouplens.lenskit.data.text.SimpleFileRatingDAO} instead.
 */
@Deprecated
@SuppressWarnings("deprecation")
public class SimpleFileRatingDAO extends org.grouplens.lenskit.data.text.SimpleFileRatingDAO {
    @Deprecated
    public SimpleFileRatingDAO(File file, String delim, CompressionMode comp) {
        super(file, delim, comp);
    }

    @Inject
    public SimpleFileRatingDAO(@RatingFile File file, @FieldSeparator String delim) {
        super(file, delim);
    }

    /**
     * Create a DAO reading from the specified file/URL and delimiter.

     * Instead of a constructor, use a static constructor method.
     *
     * @param file      The file.
     * @param delim     The delimiter to look for in the file.
     * @param comp      Whether the input is compressed.
     * @return          A SimpleFileRatingDao Object
     * @deprecated Use {@link org.grouplens.lenskit.data.text.SimpleFileRatingDAO#create(File, String, CompressionMode)} instead.
     */
    @Deprecated
    public static SimpleFileRatingDAO create(File file, String delim, CompressionMode comp){
        SimpleFileRatingDAO sfrd = new SimpleFileRatingDAO(file,delim,comp);
        return sfrd;
    }

    /**
     * Create a DAO reading from the specified file and delimiter.
     * Instead of a constructor, use a static constructor method.
     *
     * @param file      The file.
     * @param delim     The delimiter to look for in the file.
     * @return          A SimpleFileRatingDao Object
     * @deprecated Use {@link org.grouplens.lenskit.data.text.SimpleFileRatingDAO#create(File, String)} instead.
     */
    @Deprecated
    public static SimpleFileRatingDAO create(File file, String delim){
        return create(file, delim, CompressionMode.AUTO);
    }
}

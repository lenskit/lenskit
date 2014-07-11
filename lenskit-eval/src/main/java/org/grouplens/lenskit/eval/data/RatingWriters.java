/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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
package org.grouplens.lenskit.eval.data;

import org.grouplens.lenskit.data.dao.packed.BinaryFormatFlag;
import org.grouplens.lenskit.data.dao.packed.BinaryRatingPacker;
import org.grouplens.lenskit.util.table.writer.CSVWriter;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class RatingWriters {
    private RatingWriters() {}

    /**
     * Write ratings to a CSV file.
     * @param file The file to write to.
     * @return The rating writer.
     * @throws IOException if there is an error opening the file.
     */
    public static RatingWriter csv(File file) throws IOException {
        return new CSVRatingWriter(CSVWriter.open(file, null));
    }

    /**
     * Write ratings to a CSV file.
     * @param file The file to write to.
     * @param ts Whether or not to include timestamps.
     * @return The rating writer.
     * @throws IOException if there is an error opening the file.
     */
    public static RatingWriter csv(File file, boolean ts) throws IOException {
        CSVRatingWriter writer = new CSVRatingWriter(CSVWriter.open(file, null));
        writer.setIncludeTimestamps(ts);
        return writer;
    }

    /**
     * Write ratings to a packed file.
     * @param file The file to write to.
     * @return The rating writer.
     * @throws IOException if there is an error opening the file.
     */
    public static RatingWriter packed(File file, BinaryFormatFlag... flags) throws IOException {
        return new PackedRatingWriter(BinaryRatingPacker.open(file, flags));
    }

    /**
     * Write ratings to a packed file.
     * @param file The file to write to.
     * @return The rating writer.
     * @throws IOException if there is an error opening the file.
     */
    public static RatingWriter packed(File file, EnumSet<BinaryFormatFlag> flags) throws IOException {
        return new PackedRatingWriter(BinaryRatingPacker.open(file, flags));
    }
}

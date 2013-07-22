/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
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
/**
 *
 */
package org.grouplens.lenskit.data.dao;

import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;

/**
 * Rating-only data source backed by a simple delimited file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class SimpleFileRatingDAO implements EventDAO {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileRatingDAO.class);

    private final File sourceFile;
    private final String delimiter;
    private CompressionMode compression;

    /**
     * Create a URL reading from the specified file/URL and delimiter.
     *
     * @param file      The file (if {@code null}, the URL is used).
     * @param delim The delimiter to look for in the file.
     * @param comp      Whether the input is compressed.
     */
    public SimpleFileRatingDAO(File file, String delim, CompressionMode comp) {
        sourceFile = file;
        delimiter = delim;
        compression = comp;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    @Override
    public Cursor<Event> streamEvents() {
        return streamEvents(Event.class, SortOrder.ANY);
    }

    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type) {
        return streamEvents(type, SortOrder.ANY);
    }

    @SuppressWarnings({"PMD.AvoidCatchingThrowable", "unchecked"})
    @Override
    public <E extends Event> Cursor<E> streamEvents(Class<E> type, SortOrder order) {
        // if they don't want ratings, they get nothing
        if (!type.isAssignableFrom(Rating.class)) {
            return Cursors.empty();
        }

        Comparator<Event> comp = order.getEventComparator();

        Reader input = null;
        final String name = sourceFile.getPath();
        logger.debug("Opening {}", sourceFile.getPath());
        try {
            input = LKFileUtils.openInput(sourceFile, compression);

            // failing to close buffer & cursor in event of unlikely errors is OK
            BufferedReader buf;
            buf = new BufferedReader(input);
            Cursor<Rating> cursor;
            cursor = new DelimitedTextRatingCursor(buf, name, delimiter);
            if (comp == null) {
                return (Cursor<E>) cursor;
            } else {
                return (Cursor<E>) Cursors.sort(cursor, comp);
            }
        } catch (Throwable th) {
            // we got an exception, make sure we close the underlying reader since we won't be
            // returning a closeable. Otherwise we might leak file handles.
            if (input != null) {
                try {
                    Closeables.close(input, true);
                } catch (IOException ioe) {
                    // should never happen, we suppress exceptions
                    throw new DataAccessException(ioe);
                }
            }
            Throwables.propagateIfPossible(th);
            throw new DataAccessException(th);
        }
    }
}

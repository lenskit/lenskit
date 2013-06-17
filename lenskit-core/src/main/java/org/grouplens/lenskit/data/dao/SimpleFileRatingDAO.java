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

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.io.Closeables;
import org.grouplens.lenskit.cursors.Cursor;
import org.grouplens.lenskit.cursors.Cursors;
import org.grouplens.lenskit.data.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.util.io.CompressionMode;
import org.grouplens.lenskit.util.io.LKFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Comparator;
import java.util.List;

/**
 * Rating-only data source backed by a simple delimited file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @compat Public
 */
public class SimpleFileRatingDAO extends AbstractDataAccessObject {
    /**
     * Factory for opening DAOs from a file.  It assumes that the file is
     * unchanging, so {@link #snapshot()} just calls {@link #create()}.
     */
    public static class Factory implements DAOFactory {
        private final File file;
        private final String delimiter;
        private CompressionMode compression;

        /**
         * Create a new DAO factory from a file.
         *
         * @param file      The name of the file toopen.
         * @param delimiter The delimiter to use.
         * @param comp      Whether the file is compressed.
         */
        public Factory(File file, String delimiter, CompressionMode comp) {
            compression = comp;
            this.file = file;
            this.delimiter = delimiter;
        }

        /**
         * Create a factory inferring compression from the file name.
         *
         * @param file      The file name to read from.
         * @param delimiter The delimiter.
         * @see #Factory(File, String, CompressionMode)
         */
        public Factory(File file, String delimiter) {
            this(file, delimiter, CompressionMode.AUTO);
        }

        /**
         * Open a file with the delimiter read from the {@code lenskit.delimiter}
         * property (defaults to "\t" if not found).
         *
         * @param file The file to read.
         * @throws java.io.FileNotFoundException if {@code file} is not found.
         * @see #Factory(File, String, CompressionMode)
         */
        public Factory(File file) throws FileNotFoundException {
            this(file, System.getProperty("lenskit.delimiter", "\t"));
        }

        /**
         * Create a caching DAO factory.
         * @param file The input file
         * @param delimiter The text delimiter
         * @return A factory that caches the data in memory.
         * @see #Factory(java.io.File, String)
         */
        public static DAOFactory caching(File file, String delimiter) {
            final Factory f = new Factory(file, delimiter);
            Supplier<List<Rating>> sup = new Supplier<List<Rating>>() {
                @Override
                public List<Rating> get() {
                    DataAccessObject dao = f.create();
                    try {
                        return Cursors.makeList(dao.getEvents(Rating.class));
                    } finally {
                        dao.close();
                    }
                }
            };
            return new EventCollectionDAO.SoftFactory(sup);
        }

        public String getDelimiter() {
            return delimiter;
        }

        @Override
        public SimpleFileRatingDAO create() {
            return new SimpleFileRatingDAO(file, delimiter, compression);
        }

        @Override
        public SimpleFileRatingDAO snapshot() {
            return create();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleFileRatingDAO.class);

    private final File file;
    private final String delimiter;
    private CompressionMode compression;

    /**
     * Create a URL reading from the specified file/URL and delimiter.
     *
     * @param file      The file (if {@code null}, the URL is used).
     * @param delimiter The delimiter to look for in the file.
     * @param comp      Whether the input is compressed.
     */
    public SimpleFileRatingDAO(File file, String delimiter, CompressionMode comp) {
        this.file = file;
        this.delimiter = delimiter;
        compression = comp;
    }

    public File getFile() {
        return file;
    }

    @Override
    public Cursor<? extends Event> getEvents() {
        return getEvents(Event.class, SortOrder.ANY);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Event> Cursor<E> getEvents(Class<E> type, SortOrder order) {
        // if they don't want ratings, they get nothing
        if (!type.isAssignableFrom(Rating.class)) {
            return Cursors.empty();
        }

        @SuppressWarnings("rawtypes")
        Comparator comp = getComparator(order);

        Reader input = null;
        final String name = file.getPath();
        logger.debug("Opening {}", file.getPath());
        try {
            input = LKFileUtils.openInput(file, compression);

            // failing to close buffer & cursor in event of unlikely errors is OK
            BufferedReader buf;
            buf = new BufferedReader(input);
            Cursor<Rating> cursor;
            cursor = new DelimitedTextRatingCursor(buf, name, delimiter);
            if (comp == null) {
                return (Cursor<E>) cursor;
            } else {
                return Cursors.sort(cursor, comp);
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

    @Override
    public void close() {
        // do nothing, each file stream is closed by the cursor
    }
}

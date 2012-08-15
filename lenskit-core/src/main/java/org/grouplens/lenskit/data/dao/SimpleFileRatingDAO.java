/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

/**
 * Rating-only data source backed by a simple delimited file.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
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
         * @param file The name of the file toopen.
         * @param delimiter The delimiter to use.
         * @param comp Whether the file is compressed.
         */
        public Factory(File file, String delimiter, CompressionMode comp) {
            compression = comp;
            this.file = file;
            this.delimiter = delimiter;
        }

        /**
         * Create a factory inferring compression from the file name.
         * @param file The file name to read from.
         * @param delimiter The delimiter.
         * @see #Factory(File, String, CompressionMode)
         */
        public Factory(File file, String delimiter) {
            this(file, delimiter, CompressionMode.AUTO);
        }

        /**
         * Open a file with the delimiter read from the <tt>lenskit.delimiter</tt>
         * property (defaults to "\t" if not found).
         * @param file The file to read.
         * @see #Factory(File,String,CompressionMode)
         * @throws java.io.FileNotFoundException if {@code file} is not found.
         */
        public Factory(File file) throws FileNotFoundException {
            this(file, System.getProperty("lenskit.delimiter", "\t"));
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
     * @param file The file (if <tt>null</tt>, the URL is used).
     * @param delimiter The delimiter to look for in the file.
     * @param comp Whether the input is compressed.
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
        if (!type.isAssignableFrom(Rating.class))
            return Cursors.empty();

        @SuppressWarnings("rawtypes")
        Comparator comp = getComparator(order);

        Reader input;
        final String name = file.getPath();
        try {
            logger.debug("Opening {}", file.getPath());
            input = LKFileUtils.openInput(file, compression);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader buf;
        try {
            buf = new BufferedReader(input);
        } catch (RuntimeException e) {
            LKFileUtils.close(logger, input);
            throw e;
        }
        Cursor<Rating> cursor;
        try {
            cursor = new DelimitedTextRatingCursor(buf, name, delimiter);
        } catch (RuntimeException e) {
            LKFileUtils.close(logger, buf);
            throw e;
        }
        if (comp == null) {
            return (Cursor<E>) cursor;
        } else {
            return (Cursor) Cursors.sort(cursor, comp);
        }
    }

    @Override
    public void close() {
        // do nothing, each file stream is closed by the cursor
    }
}

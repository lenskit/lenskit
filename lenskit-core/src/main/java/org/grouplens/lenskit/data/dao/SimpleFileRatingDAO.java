/*
 * LensKit, a reference implementation of recommender algorithms.
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
/**
 *
 */
package org.grouplens.lenskit.data.dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Scanner;

import org.grouplens.common.cursors.Cursor;
import org.grouplens.common.cursors.Cursors;
import org.grouplens.lenskit.data.SortOrder;
import org.grouplens.lenskit.data.event.Event;
import org.grouplens.lenskit.data.event.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rating-only data source backed by a simple delimited file.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleFileRatingDAO extends AbstractDataAccessObject {
    /**
     * Factory for opening DAOs from a file.
     */
    public static class Factory implements DAOFactory {
        private final File file;
        private final String delimiter;
        private final URL url;
        
        /**
         * Create a new DAO factory from a file.
         * @param file The name of the file toopen.
         * @param delimiter The delimiter to use.
         * @throws FileNotFoundException if the file is not found.
         */
        public Factory(File file, String delimiter) throws FileNotFoundException {
            this.file = file;
            if (!file.exists())
                throw new FileNotFoundException(file.toString());
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            this.delimiter = delimiter;
        }
        
        /**
         * Open a file with the delimiter read from the <tt>lenskit.delimiter</tt>
         * property (defaults to "\t" if not found).
         * @see #Factory(File,String)
         */
        public Factory(File file) throws FileNotFoundException {
            this(file, System.getProperty("lenskit.delimiter", "\t"));
        }

        /**
         * Create a new DAO factory bound to a URL.  The delimiter is read from
         * the property <tt>lenskit.delimiter</tt>, defaulting to "\t".
         * @see #Factory(URL,String)
         */
        public Factory(URL url) {
            this(url, System.getProperty("lenskit.delimiter", "\t"));
        }

        /**
         * Create a new factory opening DAOs from the specified URL.
         * @param url The URL of the data file.
         * @param delimiter The field delimiter for parsing the file.
         */
        public Factory(URL url, String delimiter) {
            this.url = url;
            if (url.getProtocol().equals("file"))
                file = new File(url.getPath());
            else
                file = null;
            this.delimiter = delimiter;
        }
        
        @Override
        public SimpleFileRatingDAO create() {
            return new SimpleFileRatingDAO(file, url, delimiter);
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileRatingDAO.class);
    
    private final File file;
    private final URL url;
    private final String delimiter;

    /**
     * Create a URL reading from the specified file/URL and delimiter.
     * @param file The file (if <tt>null</tt>, the URL is used).
     * @param url The URL (ignored if <var>file</var> is not <tt>null</tt>).
     * @param delimiter
     */
    public SimpleFileRatingDAO(File file, URL url, String delimiter) {
        this.file = file;
        this.url = url;
        this.delimiter = delimiter;
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        return url;
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
        
        Scanner scanner;
        String name = null;
        try {
            if (file != null) {
                logger.debug("Opening {}", file.getPath());
                name = file.getPath();
                scanner = new Scanner(file);
            } else {
                logger.debug("Opening {}", url.toString());
                name = url.toString();
                InputStream instr = url.openStream();
                scanner = new Scanner(instr);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Cursor<Rating> cursor = new ScannerRatingCursor(scanner, name, delimiter);
        if (comp == null)
            return (Cursor<E>) cursor;
        else
            return Cursors.sort(cursor, comp);
    }
    
    @Override
    public void close() {
        // do nothing, each file stream is closed by the cursor
    }
}

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
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.grouplens.common.cursors.AbstractCursor;
import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.grouplens.lenskit.data.SimpleRating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source backed by a simple delimited file.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleFileDAO extends AbstractRatingDataAccessObject {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileDAO.class);
    private final File file;
    private final URL url;
    private final Pattern splitter;

    public SimpleFileDAO(File file, String delimiter) throws FileNotFoundException {
        this.file = file;
        if (!file.exists())
            throw new FileNotFoundException(file.toString());
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        splitter = Pattern.compile(Pattern.quote(delimiter));
    }

    public SimpleFileDAO(File file) throws FileNotFoundException {
        this(file, System.getProperty("org.grouplens.lenskit.bench.SimpleFileDataSource.delimiter", "\t"));
    }

    public SimpleFileDAO(URL url) {
        this(url, System.getProperty("org.grouplens.lenskit.bench.SimpleFileDataSource.delimiter", "\t"));
    }

    public SimpleFileDAO(URL url, String delimiter) {
        this.url = url;
        if (url.getProtocol().equals("file"))
            file = new File(url.getPath());
        else
            file = null;
        splitter = Pattern.compile(Pattern.quote(delimiter));
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        return url;
    }
    
    @Override
    public RatingDataSession getSession() {
        return new Session();
    }
    
    class Session extends AbstractRatingDataSession {
        public Session() {
            super(SimpleFileDAO.this);
        }

        /* (non-Javadoc)
         * @see org.grouplens.lenskit.data.dao.RatingDataAccessObject#getRatings()
         */
        @Override
        public Cursor<Rating> getRatings() {
            Scanner scanner;
            try {
                if (file != null) {
                    logger.debug("Opening {}", file.getPath());
                    scanner = new Scanner(file);
                } else {
                    logger.debug("Opening {}", url.toString());
                    InputStream instr = url.openStream();
                    scanner = new Scanner(instr);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return new RatingScannerCursor(scanner);
        }

    }
    
    class RatingScannerCursor extends AbstractCursor<Rating> {
        private Scanner scanner;
        private int lineno;
        private Rating rating;

        public RatingScannerCursor(Scanner s) {
            lineno = 0;
            scanner = s;
            rating = null;
        }

        @Override
        public void close() {
            if (scanner != null)
                scanner.close();
            scanner = null;
            rating = null;
        }

        @Override
        public boolean hasNext() {
            if (scanner == null) return false;

            while (rating == null && scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineno += 1;
                String[] fields = splitter.split(line);
                if (fields.length < 3) {
                    logger.error("Invalid input at {} line {}, skipping",
                            file, lineno);
                    continue;
                }
                long uid = Long.parseLong(fields[0]);
                long iid = Long.parseLong(fields[1]);
                double r = Double.parseDouble(fields[2]);
                long ts = -1;
                if (fields.length >= 4)
                    ts = Long.parseLong(fields[3]);

                rating = new SimpleRating(uid, iid, r, ts);
            }
            return rating != null;
        }

        @Override
        public Rating next() {
            if (!hasNext()) throw new NoSuchElementException();
            Rating r = rating;
            rating = null;
            return r;
        }

    }
}

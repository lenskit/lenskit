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

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


import org.grouplens.common.cursors.Cursor;
import org.grouplens.lenskit.data.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data source backed by a simple delimited file.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleFileDAO extends AbstractRatingDataAccessObject<Closeable> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleFileDAO.class);
    private final File file;
    private final URL url;
    private final String delimiter;

    public SimpleFileDAO(File file, String delimiter) throws FileNotFoundException {
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

    public SimpleFileDAO(File file) throws FileNotFoundException {
        this(file, System.getProperty("lenskit.delimiter", "\t"));
    }

    public SimpleFileDAO(URL url) {
        this(url, System.getProperty("lenskit.delimiter", "\t"));
    }

    public SimpleFileDAO(URL url, String delimiter) {
        this.url = url;
        if (url.getProtocol().equals("file"))
            file = new File(url.getPath());
        else
            file = null;
        this.delimiter = delimiter;
    }

    public File getFile() {
        return file;
    }

    public URL getURL() {
        return url;
    }
    
    @Override
    public Cursor<Rating> getRatings() {
        checkSession();
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
        return new ScannerRatingCursor(scanner, name, delimiter);
    }
    
    @Override
    protected Closeable openNewSession() {
        return new DummySession();
    }
}

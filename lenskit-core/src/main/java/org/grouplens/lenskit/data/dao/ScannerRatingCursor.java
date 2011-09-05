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
package org.grouplens.lenskit.data.dao;

import java.util.Scanner;
import java.util.regex.Pattern;

import javax.annotation.WillCloseWhenClosed;

import org.grouplens.lenskit.data.event.AbstractRatingCursor;
import org.grouplens.lenskit.data.event.MutableRating;
import org.grouplens.lenskit.data.event.Rating;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScannerRatingCursor extends AbstractRatingCursor<Rating> {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    private Scanner scanner;
    private final String fileName;
    private final Pattern splitter;
    private int lineno;
    private MutableRating rating;

    public ScannerRatingCursor(@WillCloseWhenClosed Scanner s) {
        this(s, null, System.getProperty("lenskit.delimiter", "\t"));
    }

    public ScannerRatingCursor(@WillCloseWhenClosed Scanner s, String name,
                               String delimiter) {
        fileName = name;
        lineno = 0;
        scanner = s;
        rating = new MutableRating();
        splitter = Pattern.compile(Pattern.quote(delimiter));
    }

    @Override
    public void close() {
        if (scanner != null)
            scanner.close();
        scanner = null;
        rating = null;
    }

    @Override
    protected Rating poll() {
        if (scanner == null) return null;

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            lineno += 1;
            String[] fields = splitter.split(line);
            if (fields.length < 3) {
                logger.error("Invalid input at {} line {}, skipping",
                             fileName, lineno);
                continue;
            }
            rating.setId(lineno);
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
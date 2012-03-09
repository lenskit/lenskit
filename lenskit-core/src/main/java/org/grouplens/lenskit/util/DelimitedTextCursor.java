/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.lenskit.util;

import com.google.common.io.Closeables;
import org.grouplens.lenskit.cursors.AbstractPollingCursor;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Cursor that reads rows of delimited text from a scanner.
 * @author Michael Ekstrand
 * @since 0.10
 */
public class DelimitedTextCursor extends AbstractPollingCursor<String[]> {
    private BufferedReader input;
    private Pattern delimiter;
    private int lineNumber = 0;

    /**
     * Construct a cursor reading text from a scanner with a regex delimiter.
     * @param in The input scanner.
     * @param delim The delimiter.
     */
    public DelimitedTextCursor(@WillCloseWhenClosed @Nonnull BufferedReader in,
                               @Nonnull Pattern delim) {
        input = in;
        delimiter = delim;
    }

    /**
     * Construct a cursor reading text from a scanner with a fixed delimiter.
     * @param in The scanner to read from.
     * @param delim The delimiter string.
     */
    public DelimitedTextCursor(@WillCloseWhenClosed @Nonnull BufferedReader in,
                               @Nonnull String delim) {
        this(in, Pattern.compile(Pattern.quote(delim)));
    }

    public String[] poll() {
        String line;
        try {
            line = input.readLine();
        } catch (IOException e) {
            throw new RuntimeException("error reading line", e);
        }
        if (line == null) {
            return null;
        } else {
            lineNumber++;
            return delimiter.split(line);
        }
    }

    /**
     * Return the number of the line returned by the last call to {@link #next()}.
     * @return The number of the last line retrieved.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void close() {
        Closeables.closeQuietly(input);
    }
}

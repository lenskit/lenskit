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
package org.grouplens.lenskit.util;

import com.google.common.base.Throwables;
import org.grouplens.lenskit.cursors.AbstractPollingCursor;

import javax.annotation.Nonnull;
import javax.annotation.WillCloseWhenClosed;
import java.io.*;

/**
 * Cursor that reads lines from a file.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class LineCursor extends AbstractPollingCursor<String> {
    private BufferedReader input;
    private int lineNumber = 0;

    /**
     * Construct a cursor reading text from a scanner with a regex delimiter.
     *
     * @param in    The input scanner.
     */
    public LineCursor(@WillCloseWhenClosed @Nonnull BufferedReader in) {
        input = in;
    }

    /**
     * Construct a delimited text cursor from a file.
     *
     * @param file  The name of the file to read.
     * @throws java.io.FileNotFoundException if {@var file} is not found.
     */
    public LineCursor(File file) throws FileNotFoundException {
        // REVIEW This doesn't handle an error constructing the BufferedReader
        this(new BufferedReader(new FileReader(file)));
    }

    @Override
    public String poll() {
        try {
            String line = input.readLine();
            lineNumber++;
            return line;
        } catch (IOException e) {
            throw new RuntimeException("error reading line", e);
        }
    }

    /**
     * Return the number of the line returned by the last call to {@link #next()}.
     *
     * @return The number of the last line retrieved.
     */
    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public void close() {
        try {
            input.close();
        } catch (IOException ex) {
            throw Throwables.propagate(ex);
        }
    }
}

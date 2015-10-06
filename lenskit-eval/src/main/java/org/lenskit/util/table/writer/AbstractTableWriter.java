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
package org.lenskit.util.table.writer;

import java.io.IOException;
import java.util.Arrays;

/**
 * Abstract helper class for implementing table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 1.1
 */
public abstract class AbstractTableWriter implements TableWriter {
    /**
     * {@inheritDoc}
     * This implementation delegates to {@link #writeRow(java.util.List)}.
     */
    @Override
    public void writeRow(Object... row) throws IOException {
        writeRow(Arrays.asList(row));
    }

    /**
     * Check the width of a row to see if it is too wide.  This formats the exception
     * with a helpful error message.
     *
     * @param width The row width.
     * @throws IllegalArgumentException if the row is too wide.
     */
    protected void checkRowWidth(int width) {
        if (width != getLayout().getColumnCount()) {
            String msg = String.format("incorrect row size (got %d of %d expected columns)",
                                       width, getLayout().getColumnCount());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * No-op close implementaiton.
     */
    @Override
    public void close() throws IOException {}
}

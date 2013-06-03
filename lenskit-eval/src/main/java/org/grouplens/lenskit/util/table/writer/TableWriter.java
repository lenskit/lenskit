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
package org.grouplens.lenskit.util.table.writer;

import org.grouplens.lenskit.util.table.TableLayout;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Write rows to a table.
 *
 * <p>
 * Instances of this class are used to actually write rows to a table. Once the
 * table has finished, call {@link #close()} to finish the table and close the
 * underlying output.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public interface TableWriter extends Closeable {
    /**
     * Get the layout of this table.
     *
     * @return The table's layout.
     */
    TableLayout getLayout();

    /**
     * Write a row to the table. This method is thread-safe.
     *
     * @param row A row of values.  If the table requires more columns, the remaining columns are
     *            empty. The row is copied if necessary; the caller is free to re-use the same array
     *            for future calls.
     * @throws IOException              if an error occurs writing the row.
     * @throws IllegalArgumentException if {@code row} has the incorrect number of columns.
     */
    void writeRow(Object... row) throws IOException;

    /**
     * Write a row to the table. This method is thread-safe.
     *
     * @param row A row of values.  If the table requires more columns, the remaining columns are
     *            empty. The row is copied if necessary; the caller is free to re-use the same array
     *            for future calls.
     * @throws IOException              if an error occurs writing the row.
     * @throws IllegalArgumentException if {@code row} has the incorrect number of columns.
     * @since 1.1
     */
    void writeRow(List<?> row) throws IOException;

    /**
     * Finish the table.  Depending on how it was constructed, some underlying
     * resource may be closed.
     */
    void close() throws IOException;
}

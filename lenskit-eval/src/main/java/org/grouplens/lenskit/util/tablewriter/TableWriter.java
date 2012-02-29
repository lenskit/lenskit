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
package org.grouplens.lenskit.util.tablewriter;

import java.io.Closeable;
import java.io.IOException;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Write rows to a table.
 * 
 * <p>
 * Instances of this class are used to actually write rows to a table. Once the
 * table has finished, call {@link #close()} to finish the table and close the
 * underlying output.
 * 
 * @see TableWriterBuilder
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
@ThreadSafe
public interface TableWriter extends Closeable {
    /**
     * @return The number of columns in the table.
     */
    int getColumnCount();

    /**
     * Write a row to the table. This method is thread-safe.
     * @param row A row of values.  If the table requires more columns, the
     * remaining columns are org.grouplens.lenskit.eval.config.empty.
     * @throws RuntimeException if {@code row} has more items than the table has
     * columns.
     */
    void writeRow(String[] row) throws IOException;

    /**
     * Finish the table.  Depending on how it was constructed, some underlying
     * device may be closed.
     */
    void close() throws IOException;
}

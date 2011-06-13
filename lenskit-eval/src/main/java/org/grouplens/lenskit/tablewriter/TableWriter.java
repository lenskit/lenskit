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
package org.grouplens.lenskit.tablewriter;

import java.io.IOException;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Write rows to a table.
 *
 * <p>Instances of this class are used to actually produce rows for a table.  There
 * are two interfaces provided: the {@link #writeRow(String[])}, {@link #writeRow(Map)}, 
 * and {@link #writeRow(Object...)} methods write an entire row at a time, while 
 * the {@link #startRow()}, {@link #setValue(int, String)} and {@link #finishRow()}
 *  methods build up a row and then write it out.
 *
 * <p>Once the table has finished, call {@link #finish()} to finish the table and
 * close the underlying output.
 * 
 * <p>TableWriter is thread safe, but only one thread can be building a row
 * at a time.  {@link #writeRow(String[])} and related methods write a row atomically,
 * while {@link #startRow()} and {@link #finishRow()} manage a lock.
 *
 * @see TableWriterBuilder
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ThreadSafe
public interface TableWriter {
    /**
     * @return The number of columns in the table.
     */
    int getColumnCount();
    
    /**
     * Start a row. Once the row is started, the current thread has a lock on
     * the table writer until the row is finished or cancelled.
     */
    void startRow();
    
    /**
     * Cancel a row, releasing the lock.
     */
    void cancelRow();
    
    /**
     * Finish and output the current row. Releases the lock (even if the row
     * writing fails!).
     */
    void finishRow() throws IOException;

    /**
     * Write a row to the table. This method is thread-safe.
     * @param row A row of values.  If the table requires more columns, the
     * remaining columns are empty.
     * @throws RuntimeException if {@code row} has more items than the table has
     * columns.
     */
    void writeRow(String[] row) throws IOException;

    /**
     * Write a row to the table.  This extracts the row values from a key-value
     * map.
     * @param data A mapping from column names to values.  Columns not defined
     * are left empty.  The string representation of each entry is used.
     * @see #writeRow(String[])
     */
    <V> void writeRow(Map<String,V> data) throws IOException;

    /**
     * Write a row to the table.
     * @see #writeRow(String[])
     */
    void writeRow(Object... columns) throws IOException;

    /**
     * Set a column in the current row to an integer value.
     * @param col The column to set
     * @param val The value to store
     * @throws IndexOutOfBoundsException if <var>col</var> is not a valid column.
     */
    void setValue(int col, long val);
    
    /**
     * Set a column in the current row to an floating-point value.
     * @param col The column to set
     * @param val The value to store
     * @throws IndexOutOfBoundsException if <var>col</var> is not a valid column.
     */
    void setValue(int col, double val);
    
    /**
     * Set a column in the current row to a string value.
     * @param col The column to set
     * @param val The value to store
     * @throws IndexOutOfBoundsException if <var>col</var> is not a valid column.
     */
    void setValue(int col, String val);
    

    /**
     * Finish the table.  Depending on how it was constructed, some underlying
     * device may be closed.  All pending rows must be finished.
     */
    void finish() throws IOException;
}

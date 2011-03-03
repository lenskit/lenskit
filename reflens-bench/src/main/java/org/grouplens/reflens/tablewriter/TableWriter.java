/*
 * RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.tablewriter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Write rows to a table.
 * 
 * Instances of this class are used to actuall produce rows for a table.  There
 * are two interfaces provided: the {@link #writeRow(List)} and {@link #writeRow(Map)}
 * methods write an entire row at a time, while the {@link #setValue(int, String)}
 * and {@link #finishRow()} methods build up a row and then write it out.
 * 
 * Once the table has finished, call {@link #finish()} to finish the table and
 * close the underlying output.
 * 
 * @see TableWriterBuilder
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface TableWriter {
	/**
	 * Query the number of columns in the table.
	 * @return
	 */
	int getColumnCount();
	
	/**
	 * Write a row to the table.  If a row is already in progress, it is
	 * finished first.
	 * @param row A row of values.  If the table requires more columns, the
	 * remaining columns are empty.
	 * @raises RuntimeException if {@code row} has more items than the table has
	 * columns.
	 */
	void writeRow(List<String> row) throws IOException;
	
	/**
	 * Write a row to the table.  This extracts the row values from a key-value
	 * map.
	 * @param data A mapping from column names to values.  Columns not defined
	 * are left empty.  The string representation of each entry is used.
	 */
	<V> void writeRow(Map<String,V> data) throws IOException;
	
	/**
	 * Write a row to the table.
	 * @see #writeRow(List)
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
	 * Finish and output the current row.
	 */
	void finishRow() throws IOException;
	
	/**
	 * Finish the table.  Depending on how it was constructed, some underlying
	 * device may be closed.
	 */
	void finish() throws IOException;
}

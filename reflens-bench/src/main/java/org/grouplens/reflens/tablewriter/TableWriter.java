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

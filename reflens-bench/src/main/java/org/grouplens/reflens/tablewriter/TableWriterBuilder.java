package org.grouplens.reflens.tablewriter;

import java.io.IOException;
import java.io.Writer;

/**
 * Build a {@link TableWriter} to output a data table.
 * 
 * The table writer framework is used to generate data tables containing
 * benchmark results.  It is an abstraction to generate things like CSV files.
 * You first set up the table writer with a {@code TableWriterBuilder} with your
 * columns, and then get a {@link TableWriter} to write the actual rows.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface TableWriterBuilder {
	/**
	 * Add a column with the given name (will be output in the header).
	 * @param name The column's name.
	 * @return The column's index (0-based).
	 */
	int addColumn(String name);
	
	/**
	 * Get a {@link TableWriter} so you can start writing rows.  Table writers
	 * will automatically start writing their header when this method is called.
	 * The writer is <b>not</b> closed when the table writer is finished, but
	 * it is flushed.
	 * @return The writer to write table rows.
	 */
	TableWriter makeWriter(Writer output) throws IOException;
}

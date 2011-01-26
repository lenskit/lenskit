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

/*
 * RefLens, a reference implementation of recommender algorithms.
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
import java.io.Writer;

import javax.annotation.WillClose;

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
     * @return The writer to write table rows.
     */
    TableWriter makeWriter(@WillClose Writer output) throws IOException;
}

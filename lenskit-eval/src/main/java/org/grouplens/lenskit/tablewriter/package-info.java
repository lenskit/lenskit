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
/**
 * Utilities for formatting and writing tabular output.
 * 
 * <p>This package provides the Table Writer framework, a mechanism for writing
 * tabular output such as CSV files.  It's used by the benchmarking code to 
 * provide its output.</p>
 * 
 * The table writer framework consists of two primary interfaces:
 * {@link TableWriterBuilder} is used to set
 * up the format of a table (e.g. the number and titles of the columns).  Once
 * the table is set up, the client code obtains a {@link TableWriter} which it
 * then uses to write the actual table data.
 */
package org.grouplens.lenskit.tablewriter;
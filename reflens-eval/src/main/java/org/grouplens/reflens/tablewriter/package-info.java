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
package org.grouplens.reflens.tablewriter;
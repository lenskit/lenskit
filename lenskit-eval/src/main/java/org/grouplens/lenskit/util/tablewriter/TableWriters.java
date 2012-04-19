/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

/**
 * Utility methods for table writers.
 * 
 * @since 0.8
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public final class TableWriters {
    /**
     * Create a table writer that writes data with common leading columns to an
     * underlying table writer.
     * 
     * @param base The base table writer for output.
     * @param prefix The values of the leading columns in this table writer.
     * @return A table writer with
     *         <code>base.getColumnCount() - prefix.length</code> columns. Each
     *         row is prefixed with the values in <var>prefix</var>.
     * @since 0.8
     */
    public static TableWriter prefixed(TableWriter base, Object[] prefix) {
        return new PrefixedTableWriter(base, prefix);
    }
}

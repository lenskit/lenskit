/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2013 Regents of the University of Minnesota and contributors
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.util.table.writer;

import java.util.Arrays;
import java.util.List;

/**
 * Utility methods for table writers.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8
 */
public final class TableWriters {
    /**
     * Create a table writer that writes data with common leading columns to an
     * underlying table writer.
     *
     * @param base   The base table writer for output.
     * @param prefix The values of the leading columns in this table writer.
     * @return A table writer with
     *         {@code base.getColumnCount() - prefix.size()} columns. Each
     *         row is prefixed with the values in {@var prefix}.
     * @since 1.1
     */
    public static TableWriter prefixed(TableWriter base, List<?> prefix) {
        return new PrefixedTableWriter(base, prefix);
    }

    /**
     * Create a table writer that writes data with common leading columns to an
     * underlying table writer.
     *
     * @param base   The base table writer for output.
     * @param prefix The values of the leading columns in this table writer.
     * @return A table writer with
     *         {@code base.getColumnCount() - prefix.length} columns. Each
     *         row is prefixed with the values in {@var prefix}.
     * @since 0.8
     */
    public static TableWriter prefixed(TableWriter base, Object... prefix) {
        return prefixed(base, Arrays.asList(prefix));
    }
}

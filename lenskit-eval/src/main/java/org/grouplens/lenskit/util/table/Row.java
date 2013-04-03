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
package org.grouplens.lenskit.util.table;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * One row of a data table.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public interface Row extends Iterable<Object> {
    /**
     * Get the value at a particular column.
     *
     * @param key The column name.
     * @return The value at that column.
     * @throws IllegalArgumentException if {@var key} does not define a column.
     */
    @Nullable
    Object value(String key);

    /**
     * Get the value at a particular column.
     *
     * @param idx The column index.
     * @return The value at that column.
     * @throws IndexOutOfBoundsException if {@var idx} is not a valid column index.
     */
    @Nullable
    Object value(int idx);

    /**
     * Get the length of this row.
     *
     * @return The length of the row.
     */
    int length();

    /**
     * Get a view of this row as a map.
     *
     * @return A map representing this row.
     */
    Map<String,Object> asMap();
}

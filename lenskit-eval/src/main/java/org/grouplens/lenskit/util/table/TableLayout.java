/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 Regents of the University of Minnesota and contributors
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

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Collection;
import java.util.List;

/**
 * A layout for a table to be written.  Specifies the columns in the table.  Column names
 * must be unique.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
public class TableLayout {
    private final List<String> names;
    private final Object2IntMap<String> indexes;

    TableLayout(Collection<String> colNames) {
        names = ImmutableList.copyOf(colNames);
        indexes = new Object2IntOpenHashMap<String>(names.size());
        for (String col: names) {
            indexes.put(col, indexes.size());
        }
        // set default return to -1, so we get an illegal index when looking up a nonexistent column
        indexes.defaultReturnValue(-1);
    }

    /**
     * Get the headers of the columns.
     *
     * @return The headers of the columns in the table layout.
     */
    public List<String> getColumns() {
        return names;
    }

    /**
     * Get the index of a particular column.
     *
     * @param col The column.
     * @return The index of the specified column, starting from 0.
     * @throws IllegalArgumentException if the column is not in the layout.
     */
    public int columnIndex(String col) {
        int idx = indexes.getInt(col);
        if (idx < 0) {
            throw new IllegalArgumentException(col + ": no such column");
        } else {
            return idx;
        }
    }

    /**
     * Get the number of columns in this layout.
     *
     * @return The number of columns in the table layout.
     */
    public int getColumnCount() {
        return names.size();
    }
}

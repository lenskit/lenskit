/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.util.table;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.jcip.annotations.Immutable;
import java.util.Collection;
import java.util.List;

/**
 * A layout for a table to be written.  Specifies the columns in the table.  Column names
 * must be unique.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.10
 */
@Immutable
public class TableLayout {
    private final List<String> names;
    private final Object2IntMap<String> indexes;

    TableLayout(Collection<String> colNames) {
        names = ImmutableList.copyOf(colNames);
        indexes = new Object2IntOpenHashMap<>(names.size());
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

    /**
     * Create a new row builder for this layout.
     * @return A new builder for rows using this layout.
     */
    public RowBuilder newRowBuilder() {
        return new RowBuilder(this);
    }
}

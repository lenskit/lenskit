/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
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
package org.lenskit.util.table;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Implementation of a single table row.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class RowImpl implements Row {
    private final ArrayList<Object> row;
    private final TableLayout layout;

    /**
     * Construct a new row implementation.
     * @param layout The table layout.
     * @param entries The row's contents. The array elements are copied.
     */
    public RowImpl(TableLayout layout, Object[] entries) {
        super();
        this.layout = layout;
        Preconditions.checkArgument(
                entries.length == layout.getColumnCount(),
                String.format("row has incorrect length (was %d, expected %d)",
                              entries.length, layout.getColumnCount()));
        row = new ArrayList<Object>(entries.length);
        Collections.addAll(row, entries);
    }

    @Override
    public Object value(String col) {
        return value(layout.columnIndex(col));
    }

    @Override
    public Object value(int idx) {
        // manually check index to get better error message
        Preconditions.checkElementIndex(idx, row.size(), "column");
        return row.get(idx);
    }

    @Override
    public int length() {
        return row.size();
    }

    @Override
    public Iterator<Object> iterator() {
        return Iterators.transform(layout.getColumns().iterator(),
                                   VALUE_FUNCTION);
    }

    @Override
    public Map<String,Object> asMap() {
        // FIXME Don't create a new set every time this is done.
        return Maps.asMap(Sets.newHashSet(layout.getColumns()),
                          VALUE_FUNCTION);
    }

    @Override
    public List<Object> asRow() {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                return value(index);
            }

            @Override
            public int size() {
                return length();
            }
        };
    }

    private final Function<String,Object> VALUE_FUNCTION = new Function<String,Object>() {
        @Override
        public Object apply(@Nullable String column) {
            return value(column);
        }
    };
}

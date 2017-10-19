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
        row = new ArrayList<>(entries.length);
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

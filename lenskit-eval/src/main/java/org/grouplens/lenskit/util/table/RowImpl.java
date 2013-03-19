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

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import org.grouplens.lenskit.util.tablewriter.TableLayout;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * Implementation of a single table row.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
class RowImpl implements Row {
    private final ArrayList<Object> row;
    private final TableLayout layout;

    public RowImpl(TableLayout layout, Object[] entries) {
        super();
        this.layout = layout;
        if (entries.length > layout.getColumnCount()) {
            throw new IllegalArgumentException("row has too many cells");
        }
        row = new ArrayList<Object>(entries.length);
        Collections.addAll(row, entries);
        for (int i = entries.length; i < layout.getColumnCount(); i++) {
            row.add(null);
        }
    }

    @Override
    public Object value(String col) {
        return value(layout.columnIndex(col));
    }

    @Override
    public Object value(int idx) {
        return row.get(idx);
    }

    @Override
    public int length() {
        return row.size();
    }

    @Override
    public Iterator<Object> iterator() {
        return Iterators.transform(layout.getColumns().iterator(),
                                   new Function<String, Object>() {
                                       @Nullable
                                       @Override
                                       public Object apply(@Nullable String input) {
                                           return value(layout.columnIndex(input));
                                       }
                                   });
    }
}

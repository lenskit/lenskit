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
package org.grouplens.lenskit.eval.util.table;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * The row of result stored in a HashMap. Note that the values are string.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
class RowImpl extends AbstractMap<String, Object> implements Row {
    private final ArrayList<Object> row = new ArrayList<Object>();
    private final HashMap<String, Integer> header;

    public RowImpl(HashMap<String, Integer> hdr, Object[] list) {
        super();
        this.header = hdr;
        Collections.addAll(this.row, list);
    }

    @Override
    public Object value(String key) {
        return row.get(header.get(key));
    }

    @Override
    public Object value(int idx) {
        return row.get(idx);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new EntrySet();
    }

    private class IteratorWrapper implements Function<Entry<String, Integer>, Entry<String, Object>> {
        public Entry<String, Object> apply(Entry<String, Integer> entry) {
            return Maps.immutableEntry(entry.getKey(), row.get(entry.getValue()));
        }
    }

    private class EntrySet extends AbstractSet<Entry<String, Object>>
            implements Set<Entry<String, Object>> {
        @Override
        public Iterator<Entry<String, Object>> iterator() {
            return Iterators.transform(header.entrySet().iterator(), new IteratorWrapper());
        }

        @Override
        public int size() {
            return RowImpl.this.size();
        }
    }

}

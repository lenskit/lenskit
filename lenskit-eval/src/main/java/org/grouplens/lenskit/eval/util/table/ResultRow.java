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
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * The row of result stored in a HashMap. Note that the values are string.
 *
 * @author Shuo Chang<schang@cs.umn.edu>
 */
public class ResultRow implements Row{
    private final ArrayList<Object> row = new ArrayList<Object>();
    private final HashMap<String, Integer> header;

    public ResultRow(HashMap<String, Integer> hdr, Object[] list) {
        this.header = hdr;
        Collections.addAll(this.row, list);
    }

    @Override
    public Object value(String key){
        return row.get(header.get(key));
    }

    @Override
    public Object value(int idx){
        return row.get(idx);
    }

    @Override
    public int size() {
        return row.size();
    }

    @Override
    public boolean isEmpty() {
        return row.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return (o instanceof String) && header.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return row.contains(o);
    }

    @Override
    public Object get(Object o) {
        return (o instanceof String) ? value((String) o):null;
    }

    @Override
    public Object put(String s, Object o) {
        throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
    }

    @Override
    public Integer remove(Object o) {
        throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
    }

    @Override
    public Set<String> keySet() {
        return header.keySet();
    }

    @Override
    public Collection<Object> values() {
        return row;
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new EntrySet();
    }

    class IteratorWrapper implements Function<Entry<String, Integer>, Entry<String, Object>> {
        public Entry<String, Object> apply(Entry<String, Integer> entry) {
            return Maps.immutableEntry(entry.getKey(), row.get(entry.getValue()));
        }
    }

    protected class EntrySet implements Set<Entry<String, Object>> {

        @Override
        public Iterator<Entry<String, Object>> iterator() {
            return Iterators.transform(header.entrySet().iterator(), new IteratorWrapper());
        }

        @Override
        public Object[] toArray() {
            return row.toArray();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public <T> T[] toArray(T[] ts) {
            return row.toArray(ts);  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean add(Entry<String, Object> stringObjectEntry) {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public boolean containsAll(Collection<?> objects) {
            boolean result = true;
            for(Object o : objects) {
                Map.Entry me = (Map.Entry)o;
                 result &= header.containsKey(me.getKey()) && row.contains(me.getValue());
            }
            return result;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<String, Object>> entries) {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public boolean retainAll(Collection<?> objects) {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public boolean removeAll(Collection<?> objects) {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Attempted to modify the table! Operation forbidden!");
        }

        @Override
        public int size() {
            return ResultRow.this.size();
        }

        @Override
        public boolean isEmpty() {
            return ResultRow.this.isEmpty();  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public boolean contains(Object o) {
            Map.Entry me = (Map.Entry)o;
            return header.containsKey(me.getKey()) && row.contains(me.getValue());
        }
    }

}

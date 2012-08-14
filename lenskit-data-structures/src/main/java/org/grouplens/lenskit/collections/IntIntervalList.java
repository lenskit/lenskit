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
package org.grouplens.lenskit.collections;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;

import java.io.Serializable;

/**
 * Efficient representation of intervals as an integer list.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @compat Public
 */
public class IntIntervalList extends AbstractIntList implements Serializable {
    private static final long serialVersionUID = -914440213158448384L;

    private final int start;
    private final int end;

    /**
     * Create the half-open interval [0,size).
     * @param size The size of the interval.
     */
    public IntIntervalList(int size) {
        this(0, size);
    }

    private static void checkIndex(int idx, int start, int end) {
        if (idx < 0 || start + idx >= end)
            throw new IndexOutOfBoundsException(String.format("%d not in [%d,%d)", idx, start, end));
    }

    /**
     * Create the half-open interval [start,end).
     * @param start The interval start point (inclusive).
     * @param end The interval end point (exclusive).
     */
    public IntIntervalList(int start, int end) {
        if (end < start)
            throw new IllegalArgumentException("end < start");
        this.start = start;
        this.end = end;
    }

    @Override
    public int getInt(int index) {
        checkIndex(index, start, end);
        return start + index;
    }

    @Override
    public int size() {
        return end - start;
    }

    /**
     * Use {@link IntIterators#fromTo(int, int)} to build an iterator.  The other
     * iterator methods in {@link AbstractIntList} delegate to this one, so this
     * is the good injection point.
     */
    @Override
    public IntListIterator listIterator(int idx) {
        checkIndex(idx, start, end + 1); // this index can be one past the end
        return IntIterators.fromTo(start + idx, end);
    }
}

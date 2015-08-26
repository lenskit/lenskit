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
package org.lenskit.util.collections;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;

import java.io.Serializable;

/**
 * Efficient representation of intervals as an integer list.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class IntIntervalList extends AbstractIntList implements Serializable {
    private static final long serialVersionUID = -914440213158448384L;

    private final int startIndex;
    private final int endIndex;

    /**
     * Create the half-open interval [0,size).
     *
     * @param size The size of the interval.
     */
    IntIntervalList(int size) {
        this(0, size);
    }

    private static void checkIndex(int idx, int start, int end) {
        if (idx < 0 || start + idx >= end) {
            throw new IndexOutOfBoundsException(String.format("%d not in [%d,%d)", idx, start, end));
        }
    }

    /**
     * Create the half-open interval [start,end).
     *
     * @param start The interval start point (inclusive).
     * @param end   The interval end point (exclusive).
     */
    IntIntervalList(int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end < start");
        }
        startIndex = start;
        endIndex = end;
    }

    @Override
    public int getInt(int index) {
        checkIndex(index, startIndex, endIndex);
        return startIndex + index;
    }

    @Override
    public int size() {
        return endIndex - startIndex;
    }

    @Override
    public IntListIterator listIterator(int idx) {
        checkIndex(idx, startIndex, endIndex + 1); // this index can be one past the end
        return IntIterators.fromTo(startIndex + idx, endIndex);
    }
}

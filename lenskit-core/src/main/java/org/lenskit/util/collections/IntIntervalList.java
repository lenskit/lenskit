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

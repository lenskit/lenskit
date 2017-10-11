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

import java.util.AbstractList;

/**
 * List consisting of multiple occurrences of one element.
 *
 * @param <T> The element type of the list.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
class RepeatedList<T> extends AbstractList<T> {
    private int size;
    private T object;

    /**
     * Construct a new repeated list.
     * @param obj The element.
     * @param sz length of the list.
     */
    public RepeatedList(T obj, int sz) {
        size = sz;
        object = obj;
    }

    @Override
    public T get(int index) {
        if (index >= 0 && index < size) {
            return object;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public int size() {
        return size;
    }
}

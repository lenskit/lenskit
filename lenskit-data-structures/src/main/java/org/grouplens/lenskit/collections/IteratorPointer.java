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
package org.grouplens.lenskit.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * A pointer wrapping an iterator.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
class IteratorPointer<E> implements Pointer<E> {
    private Iterator<E> iterator;
    private E element;
    private boolean atEnd;

    public IteratorPointer(Iterator<E> iter) {
        iterator = iter;
        if (iter.hasNext()) {
            element = iter.next();
            atEnd = false;
        } else {
            element = null;
            atEnd = true;
        }
    }

    @Override
    public boolean advance() {
        if (iterator.hasNext()) {
            element = iterator.next();
            return true;
        } else {
            element = null;
            atEnd = true;
            return false;
        }
    }

    @Override
    public E get() {
        if (atEnd) {
            throw new NoSuchElementException();
        }
        return element;
    }

    @Override
    public boolean isAtEnd() {
        return atEnd;
    }

}

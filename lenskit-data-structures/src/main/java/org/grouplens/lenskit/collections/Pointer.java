/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

/**
 * A pointer is at an element and can be advanced and range-tested. It is useful
 * in certain cases where advance/get is a more meaningful abstraction than
 * hasNext/next.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @since 0.9
 * 
 */
public interface Pointer<E> {
    /**
     * Advance the pointer.
     * 
     * @return <tt>true</tt> if the advancing yielded another element;
     *         <tt>false</tt> if the pointer is at the end after advancing.
     */
    boolean advance();
    
    /**
     * Get the current value of the pointer.
     * 
     * @return The value at the pointer.
     * @throw NoSuchElementException if the pointer is out-of-bounds.
     */
    E get();
    
    /**
     * Query whether the pointer has reached its endpoint.
     * 
     * @return <tt>true</tt> if the pointer is at the end.
     */
    boolean isAtEnd();
}

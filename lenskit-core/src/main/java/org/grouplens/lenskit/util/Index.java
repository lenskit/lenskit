/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.util;

import it.unimi.dsi.fastutil.longs.LongList;

import java.io.Serializable;

/**
 * An index mapping long IDs to consecuitive 0-based integers.  The indexes
 * fall in the range [0,{@linkplain #getObjectCount()}).
 *
 * Indexes must be serializable.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Index extends Serializable {
    /**
     * Get the index of an id.  If the object has not been interned,
     * returns a negative number.
     * @param id The id to query.
     * @return The id's index or a negative value if the id does not exist.
     */
    int getIndex(long id);
    /**
     * Get the key for an index.
     * @param idx
     * @return The ID for the given <var>idx</var>
     */
    long getId(int idx);

    int getObjectCount();
    /**
     * Get the list of IDs.
     * @return The list of (unique) IDs in the index.
     */
    LongList getIds();
}

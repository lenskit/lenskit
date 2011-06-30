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
package org.grouplens.lenskit.data.vector;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;

/**
 * Vector of data for a user (a {@link SparseVector} that is associated with
 * a particular user).
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class UserVector extends ImmutableSparseVector {
	private static final long serialVersionUID = 6027858130934920280L;
	
	private final long userId;

	public UserVector(long user, Long2DoubleMap ratings) {
		super(ratings);
		userId = user;
	}
	
	/**
     * @param user
     * @param items
     * @param values
     * @param size
     */
    public UserVector(long user, long[] items, double[] values, int size) {
        super(items, values, size);
        userId = user;
    }

    public long getUserId() {
		return userId;
	}

}

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
/**
 * 
 */
package org.grouplens.lenskit.data;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SimpleIndexedRating extends SimpleRating implements IndexedRating {
	final int userIndex, itemIndex;

	public SimpleIndexedRating(long uid, long iid, double r, long ts, int uidx, int iidx) {
		super(uid, iid, r, ts);
		userIndex = uidx;
		itemIndex = iidx;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.IndexedRating#getItemIndex()
	 */
	@Override
	final public int getItemIndex() {
		return itemIndex;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.lenskit.data.dao.IndexedRating#getUserIndex()
	 */
	@Override
	final public int getUserIndex() {
		return userIndex;
	}
	
	@Override
    public IndexedRating clone() {
	    return (IndexedRating) super.clone();
	}
}

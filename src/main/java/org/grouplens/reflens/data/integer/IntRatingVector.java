/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
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

package org.grouplens.reflens.data.integer;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.Map;

import org.grouplens.reflens.data.generic.GenericRatingVector;

/**
 * Specialized rating vector that uses fastutil for efficient storage of
 * integer-keyed ratings.
 * 
 * TODO: Profile and see if we want to expose the ability to add items without
 * boxing.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public class IntRatingVector<S> extends GenericRatingVector<S,Integer> {
	public IntRatingVector(S owner) {
		super(IntMapFactory.getInstance(), owner, null);
	}
	public IntRatingVector(S owner, Map<Integer,Float> ratings) {
		super(IntMapFactory.getInstance(), owner, ratings);
	}
	public IntRatingVector(S owner, Int2FloatMap ratings) {
		this(owner);
		this.ratings = new Int2FloatOpenHashMap(ratings);
	}
}

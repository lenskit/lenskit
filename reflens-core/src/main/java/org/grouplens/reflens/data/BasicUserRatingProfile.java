/*
 * RefLens, a reference implementation of recommender algorithms.
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
package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;

import javax.annotation.concurrent.ThreadSafe;

import org.grouplens.reflens.data.vector.SparseVector;

/**
 * Basic user rating profile backed by a collection of ratings.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@ThreadSafe
public class BasicUserRatingProfile implements UserRatingProfile {
	
	private long user;
	private Long2ObjectMap<Rating> ratings;
	private transient SoftReference<SparseVector> vector;

	/**
	 * Construct a new basic user profile.
	 * @param user The user ID.
	 * @param ratings The user's rating collection.
	 */
	public BasicUserRatingProfile(long user, Collection<Rating> ratings) {
		this.user = user;
		this.ratings = new Long2ObjectOpenHashMap<Rating>();
		for (Rating r: ratings) {
			this.ratings.put(r.getItemId(), r);
		}
	}
	
	/**
	 * Construct a profile from a map entry.
	 * @param entry
	 */
	public BasicUserRatingProfile(Map.Entry<Long, ? extends Collection<Rating>> entry) {
		this(entry.getKey(), entry.getValue());
	}

	@Override
	public double getRating(long item) {
		Rating r = ratings.get(item);
		if (r == null)
			return Double.NaN;
		else
			return r.getRating();
	}
	
	@Override
	public synchronized SparseVector getRatingVector() {
		SparseVector v = vector != null ? vector.get() : null;
		if (v == null) {
			v = Ratings.userRatingVector(getRatings());
			vector = new SoftReference<SparseVector>(v);
		}
		return v;
	}

	@Override
	public Collection<Rating> getRatings() {
		return ratings.values();
	}

	@Override
	public long getUser() {
		return user;
	}

}

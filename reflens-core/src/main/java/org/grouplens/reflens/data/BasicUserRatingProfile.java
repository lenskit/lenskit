/*
 * RefLens, a reference implementation of recommender algorithms.
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
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Map;

public class BasicUserRatingProfile implements UserRatingProfile {
	
	private long user;
	private Long2ObjectMap<Rating> ratings;

	public BasicUserRatingProfile(long user, Collection<Rating> ratings) {
		this.user = user;
		this.ratings = new Long2ObjectOpenHashMap<Rating>();
		for (Rating r: ratings) {
			this.ratings.put(r.getItemId(), r);
		}
	}
	
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
	public Collection<Rating> getRatings() {
		return ratings.values();
	}

	@Override
	public long getUser() {
		return user;
	}

}

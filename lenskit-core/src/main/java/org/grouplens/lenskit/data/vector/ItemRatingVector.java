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
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import java.util.Collection;

import org.grouplens.lenskit.data.Rating;

/**
 * Vector containing ratings for an item.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemRatingVector extends ItemVector {
	private static final long serialVersionUID = 8742253275373260663L;

	public ItemRatingVector(long item, Long2DoubleMap ratings) {
		super(item, ratings);
	}
	
	public static ItemRatingVector fromRatings(long item, Collection<? extends Rating> ratings) {
		Long2DoubleMap vect = new Long2DoubleOpenHashMap(ratings.size());
        Long2LongMap tsMap = new Long2LongOpenHashMap(ratings.size());
        tsMap.defaultReturnValue(Long.MIN_VALUE);
        for (Rating r: ratings) {
            long uid = r.getUserId();
            long ts = r.getTimestamp();
            if (ts >= tsMap.get(uid)) {
                vect.put(uid, r.getRating());
                tsMap.put(uid, ts);
            }
        }
        return new ItemRatingVector(item, vect);
	}

}

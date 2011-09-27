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
package org.grouplens.lenskit.eval.data.traintest;

import static java.lang.Math.max;

import java.util.Collections;
import java.util.List;

import org.grouplens.lenskit.data.event.Rating;
import org.grouplens.lenskit.data.event.Ratings;

/**
 * Mode for holding out ratings from a user profile.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public enum HoldoutMode {
	RANDOM {
        @Override
        public int partition(List<Rating> ratings, int n) {
            Collections.shuffle(ratings);
            return max(0, ratings.size() - n);
        }
	},
	TIMESTAMP {
	    @Override
        public int partition(List<Rating> ratings, int n) {
	        Collections.sort(ratings, Ratings.TIMESTAMP_COMPARATOR);
	        return max(0, ratings.size() - n);
	    }
	};
	
	/**
     * Partition a list of ratings. The ratings are re-arranged, and then the
     * partition point returned.
     * 
     * @param ratings The list of ratings. This may be rearranged.
     * @param n The number of ratings to hold out.
     * @return The index of the first held-out (test set) rating.
     */
	public abstract int partition(List<Rating> ratings, int n); 
	
	public static HoldoutMode fromString(String mode) {
		if (mode.equalsIgnoreCase("random")) { 
			return RANDOM;
		} else if (mode.equalsIgnoreCase("time")) {
			return TIMESTAMP;
		} else {
			throw new IllegalArgumentException("Unknown holdout mode " + mode);
		}
	}
}

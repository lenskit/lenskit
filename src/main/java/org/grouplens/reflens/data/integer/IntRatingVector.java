package org.grouplens.reflens.data.integer;

import org.grouplens.reflens.data.RatingVector;
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
public class IntRatingVector extends GenericRatingVector<Integer> {
	public IntRatingVector() {
		this(null);
	}
	public IntRatingVector(RatingVector<Integer> other) {
		super(new IntMapFactory(), other.getRatings());
	}
}

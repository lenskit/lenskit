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
public class IntRatingVector extends GenericRatingVector<Integer,Integer> {
	public IntRatingVector(Integer owner) {
		super(IntMapFactory.getInstance(), owner, null);
	}
	public IntRatingVector(Integer owner, Map<Integer,Float> ratings) {
		super(IntMapFactory.getInstance(), owner, ratings);
	}
	public IntRatingVector(Integer owner, Int2FloatMap ratings) {
		this(owner);
		this.ratings = new Int2FloatOpenHashMap(ratings);
	}
}

package org.grouplens.reflens.data.integer;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.Map;

import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.generic.GenericUserHistory;

public class IntUserHistory extends GenericUserHistory<Integer,Integer> {
	
	public IntUserHistory(int user) {
		this(user, null);
	}
	public IntUserHistory(int user, Map<Integer,Float> ratings) {
		super(new IntMapFactory(), user, ratings);
	}
}

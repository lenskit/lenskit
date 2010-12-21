package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.Iterator;

/**
 * Representation of rating vectors.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This vector class works a lot like a map, but it also caches some
 * commonly-used statistics.
 *
 */
public class RatingVector implements Iterable<Long2DoubleMap.Entry> {
	private final Long2DoubleOpenHashMap ratings;
	private Double norm;
	private Double sum;
	private Double mean;
	
	public RatingVector() {
		ratings = new Long2DoubleOpenHashMap();
		ratings.defaultReturnValue(Double.NaN);
	}
	
	protected void clearCachedValues() {
		norm = null;
		sum = null;
		mean = null;
	}
	
	/**
	 * Put a rating for <var>id</var>.  If a more recent rating for <var>id</var>
	 * exists, this rating is discarded.
	 * @param id the user or item ID
	 * @param rating the rating
	 */
	public void put(long id, double rating) {
		clearCachedValues();
		ratings.put(id, rating);
	}
	
	/**
	 * Get the rating for <var>id</var>.
	 * @param id the item or user ID for which the rating is desired
	 * @return the rating (or {@link Double.NaN} if no such rating exists)
	 */
	public double get(long id) {
		return ratings.get(id);
	}
	
	public boolean containsId(long id) {
		return ratings.containsKey(id);
	}
	
	/**
	 * Iterate over all entries.
	 * @return an iterator over all ID/Rating pairs.
	 */
	@Override
	public Iterator<Long2DoubleMap.Entry> iterator() {
		return ratings.long2DoubleEntrySet().iterator();
	}
	
	/**
	 * Fast iterator over all entries (it can reuse entry objects).
	 * @see Long2DoubleMap.FastEntrySet#fastIterator()
	 * @return a fast iterator over all ID/Rating pairs
	 */
	public Iterator<Long2DoubleMap.Entry> fastIterator() {
		return ratings.long2DoubleEntrySet().fastIterator();
	}
	
	public LongSet idSet() {
		return ratings.keySet();
	}
	
	public DoubleCollection values() {
		return ratings.values();
	}
	
	public int size() {
		return ratings.size();
	}
	
	/**
	 * Compute and return the L2 norm (Euclidian length) of the vector.
	 * @return The L2 norm of the vector
	 */
	public double norm() {
		if (norm == null) {
			double ssq = 0;
			DoubleIterator iter = values().iterator();
			while (iter.hasNext()) {
				double v = iter.nextDouble();
				ssq += v * v;
			}
			norm = Math.sqrt(ssq);
		}
		return norm;
	}
	
	/**
	 * Compute and return the L1 norm (sum) of the vector
	 * @return the sum of the vector's values
	 */
	public double sum() {
		if (sum == null) {
			double s = 0;
			DoubleIterator iter = values().iterator();
			while (iter.hasNext()) {
				s += iter.nextDouble();
			}
			sum = s;
		}
		return sum;
	}
	
	/**
	 * Compute and return the mean of the vector's values
	 * @return the mean of the vector
	 */
	public double mean() {
		if (mean == null) {
			mean = sum() / size();
		}
		return mean;
	}
}

package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.data.snapshot.RatingSnapshot;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class SlopeOneModelDataAccumulator {

	private Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> deviationMatrix;
	private Long2ObjectOpenHashMap<Long2IntOpenHashMap> coratingMatrix;
	private double damping;

	/**
	 * Creates an accumulator to process rating data and generate the necessary data for 
	 * a <tt>SlopeOneRatingPredictor</tt>.
	 * @param damping A damping term for deviation calculations.
	 * @param snapshot The rating data.
	 */
	public SlopeOneModelDataAccumulator(double damping, RatingSnapshot snapshot) {
		this.damping = damping;
		deviationMatrix = new Long2ObjectOpenHashMap<Long2DoubleOpenHashMap>();
		coratingMatrix = new Long2ObjectOpenHashMap<Long2IntOpenHashMap>();
		for (long itemId : snapshot.getItemIds()) {
			deviationMatrix.put(itemId, new Long2DoubleOpenHashMap());
			coratingMatrix.put(itemId, new Long2IntOpenHashMap());
		}
		long[] items = snapshot.getItemIds().toLongArray();
		for (int i = 0; i < items.length-1; i++) {
			for (int j = i; j < items.length; j++) {
				if (items[i] < items[j]) deviationMatrix.get(items[i]).put(items[j], Double.NaN);
				else deviationMatrix.get(items[j]).put(items[i], Double.NaN);
			}
		}
	}

	/**
	 * Provide a pair of ratings to the accumulator.
	 * @param id1 The id of the first item.
	 * @param rating1 The user's rating of the first item.
	 * @param id2 The id of the second item.
	 * @param rating2 The user's rating of the second item.
	 */
	public void putRatingPair(long id1, double rating1, long id2, double rating2) {
		if (id1 != id2) {
			if (id1 < id2) {
				if (Double.isNaN(deviationMatrix.get(id1).get(id2)))
						deviationMatrix.get(id1).put(id2, rating1 - rating2);
				else
					deviationMatrix.get(id1).put(id2, deviationMatrix.get(id1).get(id2) + (rating1 - rating2));				
				
				coratingMatrix.get(id1).put(id2, coratingMatrix.get(id1).get(id2)+1);
			}
			else {
				if (Double.isNaN(deviationMatrix.get(id2).get(id1)))
					deviationMatrix.get(id2).put(id1, rating2 - rating1);
				else
					deviationMatrix.get(id2).put(id1, deviationMatrix.get(id2).get(id1) + (rating2 - rating1));
				
				coratingMatrix.get(id2).put(id1, coratingMatrix.get(id2).get(id1)+1);
			}
		}
	}

	/**
	 * @return A matrix of item deviation values to be used by
	 * a <tt>SlopeOneRatingPredictor</tt>.
	 */
	public Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> buildDeviationMatrix() {
		for (long item1 : deviationMatrix.keySet()) {
			for (long item2 : deviationMatrix.get(item1).keySet()) {
				if (coratingMatrix.get(item1).get(item2) != 0) {
					double deviation = deviationMatrix.get(item1).get(item2)/(coratingMatrix.get(item1).get(item2) + damping);
					deviationMatrix.get(item1).put(item2, deviation);
				}
			}
		}
		return deviationMatrix;
	}

	/**
	 * @return A matrix, containing the number of co-rating users for each item
	 * pair, to be used by a <tt>SlopeOneRatingPredictor</tt>.
	 */
	public Long2ObjectOpenHashMap<Long2IntOpenHashMap> buildCoratingMatrix() {
		return coratingMatrix;
	}

}

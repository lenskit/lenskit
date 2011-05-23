package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.data.snapshot.RatingSnapshot;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

public class DeviationMatrix {
	
	private Long2ObjectOpenHashMap<Long2DoubleOpenHashMap> deviations;
	
	public DeviationMatrix(RatingSnapshot snap) {
		deviations = new Long2ObjectOpenHashMap<Long2DoubleOpenHashMap>();
		LongIterator itemId = snap.getItemIds().iterator();
		while (itemId.hasNext()) deviations.put(itemId.next(), new Long2DoubleOpenHashMap());
	}
	
	public void put (long item1, long item2, double n) {
		if (item1 != item2) {
			if (item1 < item2) deviations.get(item1).put(item2, n);
			else deviations.get(item2).put(item1, -n);
		}
	}
	
	public double get(long item1, long item2) {
		if (item1 == item2) return 0;
		else if (item1 < item2) return deviations.get(item1).get(item2);
		else return -deviations.get(item2).get(item1);
	}

}

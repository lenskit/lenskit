package org.grouplens.lenskit.slopeone;

import org.grouplens.lenskit.data.snapshot.RatingSnapshot;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongIterator;

public class CoratingMatrix {

	private Long2ObjectOpenHashMap<Long2IntOpenHashMap> commonUsers;
	
	public CoratingMatrix(RatingSnapshot snap) {
		commonUsers = new Long2ObjectOpenHashMap<Long2IntOpenHashMap>();
		LongIterator itemID = snap.getItemIds().iterator();
		while (itemID.hasNext()) commonUsers.put(itemID.next(), new Long2IntOpenHashMap());
	}
	
	public void put(long item1, long item2, int n) {
		if (item1 != item2) {
			if (item1 < item2) commonUsers.get(item1).put(item2, n);
			else commonUsers.get(item2).put(item1, n);
		}
	}
	
	public int get(long item1, long item2) {
		if (item1 != item2) {
			if (item1 < item2) return commonUsers.get(item1).get(item2);
			else return commonUsers.get(item2).get(item1);
		}
		return 0;
	}
}

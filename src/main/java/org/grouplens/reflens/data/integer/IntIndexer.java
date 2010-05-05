package org.grouplens.reflens.data.integer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import org.grouplens.reflens.data.Indexer;

public class IntIndexer implements Indexer<Integer> {
	private Int2IntMap map = new Int2IntOpenHashMap();
	private IntArrayList objects = new IntArrayList();
	
	public int getIndex(Integer obj) {
		return getIndex(obj, true);
	}
	
	public int getIndex(Integer object, boolean insert) {
		if (map.containsKey(object)) {
			return map.get(object);
		} else if (insert) {
			int idx = objects.size();
			map.put(object.intValue(), idx);
			objects.add(object);
			return idx;
		} else {
			return -1;
		}
	}
	
	public Integer getObject(int idx) {
		return objects.get(idx);
	}
	
	public int getObjectCount() {
		return objects.size();
	}
}

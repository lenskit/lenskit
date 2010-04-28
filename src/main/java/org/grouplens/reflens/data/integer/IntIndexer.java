package org.grouplens.reflens.data.integer;

import org.grouplens.reflens.data.Indexer;

import bak.pcj.list.IntArrayList;
import bak.pcj.map.IntKeyIntMap;
import bak.pcj.map.IntKeyIntOpenHashMap;

public class IntIndexer implements Indexer<Integer> {
	private IntKeyIntMap map = new IntKeyIntOpenHashMap();
	private IntArrayList objects = new IntArrayList();
	
	public int getIndex(Integer object) {
		if (map.containsKey(object)) {
			return map.get(object);
		} else {
			int idx = objects.size();
			map.put(object, idx);
			objects.add(object);
			return idx;
		}
	}
	
	public Integer getObject(int idx) {
		return objects.get(idx);
	}
}

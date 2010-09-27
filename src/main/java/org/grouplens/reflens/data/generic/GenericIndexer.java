package org.grouplens.reflens.data.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.data.Indexer;

public class GenericIndexer<T> implements Indexer<T> {
	private Map<T,Integer> map = new HashMap<T,Integer>();
	private List<T> objs = new ArrayList<T>();
	
	public int getIndex(T obj) {
		return getIndex(obj, true);
	}
	
	public int getIndex(T obj, boolean insert) {
		if (map.containsKey(obj)) {
			return map.get(obj);
		} else if (insert) {
			int idx = objs.size();
			map.put(obj, idx);
			objs.add(obj);
			return idx;
		} else {
			return -1;
		}
	}
	
	public T getObject(int idx) {
		return objs.get(idx);
	}
	
	public int getObjectCount() {
		return objs.size();
	}
}

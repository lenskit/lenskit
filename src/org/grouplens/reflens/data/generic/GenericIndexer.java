package org.grouplens.reflens.data.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grouplens.reflens.data.Indexer;

public class GenericIndexer<T> extends Object implements Indexer<T> {
	private Map<T,Integer> map = new HashMap<T,Integer>();
	private List<T> objs = new ArrayList<T>();
	
	public int getIndex(T obj) {
		if (map.containsKey(obj)) {
			return map.get(obj);
		} else {
			int idx = objs.size();
			map.put(obj, idx);
			objs.add(obj);
			return idx;
		}
	}
	
	public T getObject(int idx) {
		return objs.get(idx);
	}
}

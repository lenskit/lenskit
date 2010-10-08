/* RefLens, a reference implementation of recommender algorithms.
 * Copyright 2010 Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

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
		if (map.containsKey(obj)) {
			return map.get(obj);
		} else {
			return -1;
		}
	}
	
	public int internObject(T obj) {
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
	
	public int getObjectCount() {
		return objs.size();
	}
}

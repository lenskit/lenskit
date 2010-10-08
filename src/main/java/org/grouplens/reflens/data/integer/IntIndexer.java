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

package org.grouplens.reflens.data.integer;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import org.grouplens.reflens.data.Indexer;

public class IntIndexer implements Indexer<Integer> {
	private Int2IntMap map = new Int2IntOpenHashMap();
	private IntArrayList objects = new IntArrayList();
	
	public int getIndex(Integer obj) {
		return getIndex(obj, false);
	}
	
	private int getIndex(Integer object, boolean insert) {
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
	
	public int internObject(Integer object) {
		return getIndex(object, true);
	}
	
	public Integer getObject(int idx) {
		return objects.get(idx);
	}
	
	public int getObjectCount() {
		return objects.size();
	}
}

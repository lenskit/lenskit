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

package org.grouplens.reflens.data;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;

public class Indexer implements Index {
	private Long2IntMap indexes;
	private LongArrayList ids;
	
	public Indexer() {
		indexes = new Long2IntOpenHashMap();
		indexes.defaultReturnValue(-1);
		ids = new LongArrayList();
	}

	@Override
	public long getId(int idx) {
		return ids.getLong(idx);
	}

	@Override
	public int getIndex(long id) {
		return indexes.get(id);
	}

	@Override
	public int getObjectCount() {
		return ids.size();
	}
	
	public int internId(long id) {
		int idx = getIndex(id);
		if (idx < 0) {
			idx = ids.size();
			ids.add(id);
			indexes.put(id, idx);
		}
		return idx;
	}
}

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

package org.grouplens.reflens.svd;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SparseMatrix {
	private List<Int2FloatMap> data;
	
	public SparseMatrix() {
		data = new ObjectArrayList<Int2FloatMap>();
	}
	
	public SparseMatrix (int nrows) {
		data = new ObjectArrayList<Int2FloatMap>(nrows);
		for (int i = 0; i < nrows; i++) {
			Int2FloatMap m = new Int2FloatOpenHashMap();
			m.defaultReturnValue(Float.NaN);
			data.add(m);
		}
	}
	
	public void set(int row, int col, float value) {
		while (row >= data.size()) {
			Int2FloatMap m = new Int2FloatOpenHashMap();
			m.defaultReturnValue(Float.NaN);
			data.add(m);
		}
		data.get(row).put(col, value);
	}
	
	public float get(int row, int col) {
		if (row >= data.size()) {
			return Float.NaN;
		} else {
			return data.get(row).get(col);
		}
	}
	
	public Int2FloatMap row(int row) {
		if (row >= data.size()) {
			return Int2FloatMaps.EMPTY_MAP;
		} else {
			return Int2FloatMaps.unmodifiable(data.get(row));
		}
	}
}

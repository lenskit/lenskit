/**
 * 
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

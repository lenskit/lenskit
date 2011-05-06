package org.grouplens.lenskit.eval.predict;

import it.unimi.dsi.fastutil.longs.AbstractLongComparator;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongList;

import org.grouplens.lenskit.data.vector.SparseVector;

public class RankEvaluationUtils {

	/**
	 * Sort the keys of a vector in decreasing order by value.
	 * @param vector The vector to query.
	 * @return The {@link SparseVector#keySet()} of <var>vector</var>, sorted
	 * by value.
	 */
	public static LongList sortKeys(final SparseVector vector) {
		long[] items = vector.keySet().toLongArray();
		for (int i = 0; i < items.length; i++) {
			if (Double.isNaN(vector.get(items[i])))
				throw new RuntimeException("Unexpected NaN");
		}
		LongArrays.quickSort(items, new AbstractLongComparator() {
			@Override
			public int compare(long k1, long k2) {
				return Double.compare(vector.get(k2), vector.get(k1));
			}
		});
		return LongArrayList.wrap(items);
	}
}

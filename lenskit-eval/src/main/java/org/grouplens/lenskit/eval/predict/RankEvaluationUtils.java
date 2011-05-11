/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
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

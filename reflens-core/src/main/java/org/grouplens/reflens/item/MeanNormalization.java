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

package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.util.Collection;
import java.util.Map;

import org.grouplens.reflens.Normalizer;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MeanNormalization implements Normalizer<Long,Map<Long,Double>> {
	/**
	 * Computes the mean of the vector.
	 * @param vector
	 * @return
	 */
	private double computeMean(Map<Long,Double> vector) {
		double sum = 0.0f;
		
		// if the value collection is a double collection, we can avoid boxing
		Collection<Double> values = vector.values();
		if (values instanceof DoubleCollection) {
			DoubleCollection vfast = (DoubleCollection) values;
			DoubleIterator iter = vfast.iterator();
			while (iter.hasNext()) {
				sum += iter.nextDouble();
			}
		} else {
			for (double v: values) {
				sum += v;
			}
		}
		
		return sum / values.size();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Normalization#normalize(java.lang.Object)
	 */
	@Override
	public Map<Long,Double> normalize(Long owner, Map<Long,Double> ratings) {
		Map<Long,Double> normed = new Long2DoubleOpenHashMap();
		double mean = computeMean(ratings);
		for (Map.Entry<Long, Double> e: ratings.entrySet()) {
			normed.put(e.getKey(), e.getValue() - mean);
		}
		return normed;
	}
}

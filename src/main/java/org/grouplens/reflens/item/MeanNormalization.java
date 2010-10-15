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

import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.grouplens.reflens.Normalizer;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class MeanNormalization<S,T> implements Normalizer<S,Map<T,Float>> {
	private final Provider<Map<T,Float>> mapProvider;
	
	public MeanNormalization() {
		mapProvider = new Provider<Map<T,Float>>() {
			public Map<T,Float> get() {
				return new HashMap<T, Float>();
			}
		};
	}
	
	@Inject
	MeanNormalization(Provider<Map<T,Float>> mapP) {
		mapProvider = mapP;
	}
	
	/**
	 * Computes the mean of the vector.
	 * @param vector
	 * @return
	 */
	private float computeMean(Map<T,Float> vector) {
		float sum = 0.0f;
		
		// if the value collection is a float collection, we can avoid boxing
		Collection<Float> values = vector.values();
		if (values instanceof FloatCollection) {
			FloatCollection vfast = (FloatCollection) values;
			FloatIterator iter = vfast.iterator();
			while (iter.hasNext()) {
				sum += iter.nextFloat();
			}
		} else {
			for (Float v: values) {
				sum += v;
			}
		}
		
		return sum / values.size();
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Normalization#normalize(java.lang.Object)
	 */
	@Override
	public Map<T,Float> normalize(S owner, Map<T,Float> ratings) {
		Map<T,Float> normed = mapProvider.get();
		float mean = computeMean(ratings);
		for (Map.Entry<T, Float> e: ratings.entrySet()) {
			normed.put(e.getKey(), e.getValue() - mean);
		}
		return normed;
	}
}

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

import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;

import org.grouplens.reflens.OptimizableMapSimilarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.item.params.SimilarityDamper;

import com.google.inject.Inject;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class CosineSimilarity
	implements OptimizableMapSimilarity<Long, Double, Long2DoubleMap>, SymmetricBinaryFunction {
	
	private final double dampingFactor;
	
	public CosineSimilarity() {
		this(0.0);
	}
	
	@Inject
	public CosineSimilarity(@SimilarityDamper double dampingFactor) {
		this.dampingFactor = dampingFactor;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.Similarity#similarity(java.lang.Object, java.lang.Object)
	 */
	@Override
	public double similarity(Long2DoubleMap vec1, Long2DoubleMap vec2) {
		double dot = 0.0f;
		double ssq1 = 0.0f;
		double ssq2 = 0.0f;
		
		ObjectSet<Long2DoubleMap.Entry> v1entries = vec1.long2DoubleEntrySet();
		ObjectIterator<Long2DoubleMap.Entry> v1iter;
		try {
			v1iter = ((Long2DoubleMap.FastEntrySet) v1entries).fastIterator();
		} catch (ClassCastException e) {
			v1iter = v1entries.iterator();
		}
		while (v1iter.hasNext()) {
			Long2DoubleMap.Entry e = v1iter.next();
			long k = e.getLongKey();
			double v = e.getDoubleValue();
			if (vec2.containsKey(k)) {
				dot += v * vec2.get(k);
			}
			ssq1 += v * v;
		}
		DoubleIterator v2iter = vec2.values().iterator();
		while (v2iter.hasNext()) {
			double v = v2iter.nextDouble();
			ssq2 += v * v;
		}
		
		double denom = Math.sqrt(ssq1) * Math.sqrt(ssq2) + dampingFactor;
		if (denom == 0.0f) {
			return Double.NaN;
		} else { 
			return dot / (double) denom;
		}
	}
}

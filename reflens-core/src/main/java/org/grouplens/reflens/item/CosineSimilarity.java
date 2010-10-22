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
public class CosineSimilarity<I>
	implements OptimizableMapSimilarity<I,Double>, SymmetricBinaryFunction {
	
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
	public double similarity(Map<I,Double> vec1, Map<I,Double> vec2) {
		if (vec1 instanceof Int2DoubleMap && vec2 instanceof Int2DoubleMap)
			return fastSimilarity((Int2DoubleMap) vec1, (Int2DoubleMap) vec2);
		
		double dot = 0.0f;
		double ssq1 = 0.0f;
		double ssq2 = 0.0f;
		for (Map.Entry<I,Double> e: vec1.entrySet()) {
			I i = e.getKey();
			double v = e.getValue();
			if (vec2.containsKey(i)) {
				dot += v * vec2.get(i);
			}
			ssq1 += v * v;
		}
		for (double v: vec2.values()) {
			ssq2 += v * v;
		}
		double denom = Math.sqrt(ssq1) * Math.sqrt(ssq2) + dampingFactor;
		if (denom == 0.0f) {
			return Double.NaN;
		} else { 
			return dot / (double) denom;
		}
	}
	
	private final double fastSimilarity(Int2DoubleMap vec1, Int2DoubleMap vec2) {
		double dot = 0.0f;
		double ssq1 = 0.0f;
		double ssq2 = 0.0f;
		
		ObjectSet<Int2DoubleMap.Entry> v1entries = vec1.int2DoubleEntrySet();
		ObjectIterator<Int2DoubleMap.Entry> v1iter;
		try {
			v1iter = ((Int2DoubleMap.FastEntrySet) v1entries).fastIterator();
		} catch (ClassCastException e) {
			v1iter = v1entries.iterator();
		}
		while (v1iter.hasNext()) {
			Int2DoubleMap.Entry e = v1iter.next();
			int k = e.getIntKey();
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

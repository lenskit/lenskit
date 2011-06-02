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

import static org.junit.Assert.*;
import org.grouplens.lenskit.data.vector.MutableSparseVector;
import org.junit.Test;

public class TestHLU {
	
	@Test
	public void testComputeHLU() {

		HLUtilityEvaluator eval = new HLUtilityEvaluator(5);
		long[] items = {1, 2, 3, 4, 5, 6, 7 ,8, 9, 10};
		double[] ratings1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
		double[] ratings2 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
		double[] ratings3 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};
		MutableSparseVector v1 = MutableSparseVector.wrap(items,ratings1);
		MutableSparseVector v2 = MutableSparseVector.wrap(items, ratings2);
		MutableSparseVector v3 = MutableSparseVector.wrap(items, ratings3);
		assertEquals(eval.computeHLU(RankEvaluationUtils.sortKeys(v1), v1), 21.9232, 0.0001);
		assertEquals(eval.computeHLU(RankEvaluationUtils.sortKeys(v2),v2), 20.9661, 0.0001);
		assertEquals(eval.computeHLU(RankEvaluationUtils.sortKeys(v3),v3), 20.0381, 0.0001);
	}
	
	@Test
	public void testAccumulator() {
		
		HLUtilityEvaluator eval = new HLUtilityEvaluator(5);
		long[] items = {1, 2, 3, 4, 5, 6, 7 ,8, 9, 10};
		double[] ratings1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
		double[] predictions1 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
		double[] ratings2 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};
		double[] predictions2 = {4, 4, 5, 2, 3, 2, 3, 4, 4, 3};
		double[] ratings3 = {5, 4, 5, 3, 2, 3, 4, 5, 3, 5};
		double[] predictions3 = {4, 4, 5, 3, 3, 4, 5, 4, 4, 4};
		MutableSparseVector rate1 = MutableSparseVector.wrap(items,ratings1);
		MutableSparseVector predict1 = MutableSparseVector.wrap(items, predictions1);
		MutableSparseVector rate2 = MutableSparseVector.wrap(items, ratings2);
		MutableSparseVector predict2 = MutableSparseVector.wrap(items, predictions2);
		MutableSparseVector rate3 = MutableSparseVector.wrap(items, ratings3);
		MutableSparseVector predict3 = MutableSparseVector.wrap(items, predictions3);
		HLUtilityEvaluator.Accum acc = eval.makeAccumulator();
		acc.evaluatePredictions(1, rate1, predict1);
		assertEquals(acc.nusers, 1);
		assertEquals(acc.total, 0.9563, 0.0001);
		acc.evaluatePredictions(2, rate2, predict2);
		assertEquals(acc.nusers, 2);
		assertEquals(acc.total, 1.9397, 0.0001);
		acc.evaluatePredictions(3, rate3, predict3);
		assertEquals(acc.nusers, 3);
		assertEquals(acc.total, 2.9201, 0.0001);
	}
}

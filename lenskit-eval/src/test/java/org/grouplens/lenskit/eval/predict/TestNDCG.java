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
import org.grouplens.lenskit.eval.predict.PredictionEvaluator.Accumulator;
import org.junit.Test;


public class TestNDCG {

	@Test
	public void testComputeNDCG() {

		long[] items = {1, 2, 3, 4, 5, 6, 7 ,8, 9, 10};
		double[] ratings1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
		double[] ratings2 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
		double[] ratings3 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};
		MutableSparseVector v1 = MutableSparseVector.wrap(items,ratings1);
		MutableSparseVector v2 = MutableSparseVector.wrap(items, ratings2);
		MutableSparseVector v3 = MutableSparseVector.wrap(items, ratings3);
		assertEquals(NDCGEvaluator.computeDCG(RankEvaluationUtils.sortKeys(v1), v1), 22.0418, 0.0001);
		assertEquals(NDCGEvaluator.computeDCG(RankEvaluationUtils.sortKeys(v2),v2), 21.0954, 0.0001);
		assertEquals(NDCGEvaluator.computeDCG(RankEvaluationUtils.sortKeys(v3),v3), 20.0742, 0.0001);
	}

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
}
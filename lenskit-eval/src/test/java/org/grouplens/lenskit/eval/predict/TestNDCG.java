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
	
	@Test void testEvaluateNDCGPredictions() {
		
		long[] items = {1, 2, 3, 4, 5, 6, 7 ,8, 9, 10};
		double[] rate1 = {5, 4, 4, 3, 5, 3, 4, 3, 2, 5};
		double[] predict1 = {5, 5, 4, 4, 4, 3, 2, 2, 3, 4};
		double[] rate2 = {4, 5, 4, 2, 3, 1, 3, 4, 5, 2};
		double[] predict2 = {4, 4, 5, 3, 3, 2, 4, 4, 5, 3}; //21.4109
		MutableSparseVector ratings1 = MutableSparseVector.wrap(items,rate1);
		MutableSparseVector predictions1 = MutableSparseVector.wrap(items, predict1);
		MutableSparseVector ratings2 = MutableSparseVector.wrap(items, rate2);
		MutableSparseVector predictions2 = MutableSparseVector.wrap(items, predict2);
		Accumulator acc = (new NDCGEvaluator()).makeAccumulator();
		acc.evaluatePredictions(0, ratings1, predictions1);
		acc.evaluatePredictions(0, ratings2, predictions2);
	}

}
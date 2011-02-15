/**
 * 
 */
package org.grouplens.reflens.knn;

import static org.grouplens.common.test.MoreAsserts.assertIsEmpty;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Comparator;

import org.grouplens.reflens.knn.TruncatingSimilarityMatrixBuilder.Score;
import org.grouplens.reflens.util.IndexedItemScore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Doubles;


/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestTruncatingSimilarityMatrixBuilder {
	private static final double EPSILON = 1.0e-6;
	
	private TruncatingSimilarityMatrixBuilder builder;
	
	@Before
	public void createBuilder() {
		builder = new TruncatingSimilarityMatrixBuilder(5, 10);
	}
	
	@Test
	public void testScore() {
		Score s = new Score(5, 7);
		assertEquals(s.getIndex(), 5);
		assertEquals(s.getScore(), 7, EPSILON);
	}
	
	@Test
	public void testEmptyMatrix() {
		assertEquals(10, builder.size());
		SimilarityMatrix matrix = builder.build();
		assertEquals(10, matrix.size());
		for (int i = 0; i < 10; i++) {
			assertIsEmpty(matrix.getNeighbors(i));
		}
	}
	
	@Test
	public void testPopulateRow() {
		builder.put(0, 2, 0.2);
		builder.put(0, 3, 0.5);
		builder.put(0, 7, 0.4);
		SimilarityMatrix matrix = builder.build();
		assertEquals(10, matrix.size());
		IndexedItemScore[] neighbors = 
			Iterables.toArray(matrix.getNeighbors(0), IndexedItemScore.class);
		assertEquals(3, neighbors.length);
		Arrays.sort(neighbors, new ScoreComparator());
		assertEquals(2, neighbors[0].getIndex());
		assertEquals(7, neighbors[1].getIndex());
		assertEquals(3, neighbors[2].getIndex());
		assertEquals(0.4, neighbors[1].getScore(), EPSILON);
		for (int i = 1; i < 10; i++) {
			assertIsEmpty(matrix.getNeighbors(i));
		}
	}
	
	@Test
	public void testPopulateRowSymmetric() {
		builder.putSymmetric(0, 2, 0.2);
		builder.putSymmetric(0, 3, 0.5);
		builder.putSymmetric(0, 7, 0.4);
		SimilarityMatrix matrix = builder.build();
		assertEquals(10, matrix.size());
		IndexedItemScore[] neighbors = 
			Iterables.toArray(matrix.getNeighbors(0), IndexedItemScore.class);
		assertEquals(3, neighbors.length);
		Arrays.sort(neighbors, new ScoreComparator());
		assertEquals(2, neighbors[0].getIndex());
		assertEquals(7, neighbors[1].getIndex());
		assertEquals(3, neighbors[2].getIndex());
		assertEquals(0.4, neighbors[1].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(2), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.2, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(3), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.5, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(7), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.4, neighbors[0].getScore(), EPSILON);
	}
	
	@Test
	public void testOverflowRow() {
		builder.put(0, 2, 0.2);
		builder.put(0, 3, 0.5);
		builder.put(0, 7, 0.4);
		builder.put(0, 5, 1.0);
		builder.put(0, 4, 0.7);
		builder.put(0, 8, 0.9);
		builder.put(0, 6, 0.1);
		SimilarityMatrix matrix = builder.build();
		assertEquals(10, matrix.size());
		IndexedItemScore[] neighbors = 
			Iterables.toArray(matrix.getNeighbors(0), IndexedItemScore.class);
		assertEquals(5, neighbors.length);
		Arrays.sort(neighbors, new ScoreComparator());
		final int[] expInd = {7, 3, 4, 8, 5};
		final double[] expScore = {0.4, 0.5, 0.7, 0.9, 1.0};
		for (int i = 0; i < 5; i++) {
			assertEquals(expInd[i], neighbors[i].getIndex());
			assertEquals(expScore[i], neighbors[i].getScore(), EPSILON);
		}
	}
	
	@Test
	public void testOverflowRowSymmetric() {
		builder.putSymmetric(0, 2, 0.2);
		builder.putSymmetric(0, 3, 0.5);
		builder.putSymmetric(0, 7, 0.4);
		builder.putSymmetric(0, 5, 1.0);
		builder.putSymmetric(0, 4, 0.7);
		builder.putSymmetric(0, 8, 0.9);
		builder.putSymmetric(0, 6, 0.1);
		SimilarityMatrix matrix = builder.build();
		assertEquals(10, matrix.size());
		IndexedItemScore[] neighbors = 
			Iterables.toArray(matrix.getNeighbors(0), IndexedItemScore.class);
		assertEquals(5, neighbors.length);
		Arrays.sort(neighbors, new ScoreComparator());
		final int[] expInd = {7, 3, 4, 8, 5};
		final double[] expScore = {0.4, 0.5, 0.7, 0.9, 1.0};
		for (int i = 0; i < 5; i++) {
			assertEquals(expInd[i], neighbors[i].getIndex());
			assertEquals(expScore[i], neighbors[i].getScore(), EPSILON);
		}
		
		assertIsEmpty(matrix.getNeighbors(1));
		assertIsEmpty(matrix.getNeighbors(9));
		
		neighbors = Iterables.toArray(matrix.getNeighbors(2), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.2, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(3), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.5, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(7), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.4, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(6), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(0.1, neighbors[0].getScore(), EPSILON);
		
		neighbors = Iterables.toArray(matrix.getNeighbors(5), IndexedItemScore.class);
		assertEquals(1, neighbors.length);
		assertEquals(0, neighbors[0].getIndex());
		assertEquals(1.0, neighbors[0].getScore(), EPSILON);
	}
	
	static class ScoreComparator implements Comparator<IndexedItemScore> {
		@Override
		public int compare(IndexedItemScore arg0, IndexedItemScore arg1) {
			return Doubles.compare(arg0.getScore(), arg1.getScore());
		}
	}
}

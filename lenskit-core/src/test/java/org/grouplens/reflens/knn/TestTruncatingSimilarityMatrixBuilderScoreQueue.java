/*
 * RefLens, a reference implementation of recommender algorithms.
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
/**
 * 
 */
package org.grouplens.reflens.knn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.grouplens.reflens.knn.TruncatingSimilarityMatrixBuilder.ScoreQueue;
import org.grouplens.reflens.util.IndexedItemScore;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestTruncatingSimilarityMatrixBuilderScoreQueue {
	private static final double EPSILON = 1.0e-6;
	private ScoreQueue queue;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void createQueue() throws Exception {
		queue = new ScoreQueue(5);
	}

	@Test
	public void testEmptyQueue() {
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
	}
	
	@Test
	public void testInsertQueue() {
		queue.put(5, 0.3);
		assertFalse(queue.isEmpty());
		assertEquals(1, queue.size());
		IndexedItemScore[] scores = Iterables.toArray(queue, IndexedItemScore.class);
		assertEquals(1, scores.length);
		assertEquals(5, scores[0].getIndex());
		assertEquals(0.3, scores[0].getScore(), EPSILON);
	}
	
	@Test
	public void testInsertReject() {
		queue.put(5, 0.3);
		queue.put(6, 0.2);
		queue.put(1, 0.5);
		queue.put(2, 1.0);
		queue.put(7, 0.7);
		queue.put(9, 0.4);
		queue.put(10, 0.1);
		
		assertFalse(queue.isEmpty());
		assertEquals(5, queue.size());
		IndexedItemScore[] scores = Iterables.toArray(queue, IndexedItemScore.class);
		assertEquals(5, scores.length);
		Arrays.sort(scores, new TestTruncatingSimilarityMatrixBuilder.ScoreComparator());
		assertEquals(5, scores[0].getIndex());
		assertEquals(0.3, scores[0].getScore(), EPSILON);
		assertEquals(2, scores[4].getIndex());
		assertEquals(1.0, scores[4].getScore(), EPSILON);
	}
}

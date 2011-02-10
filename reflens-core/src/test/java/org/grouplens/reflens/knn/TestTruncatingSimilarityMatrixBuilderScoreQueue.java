/**
 * 
 */
package org.grouplens.reflens.knn;

import static org.grouplens.reflens.knn.TruncatingSimilarityMatrixBuilder.ScoreQueue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TestTruncatingSimilarityMatrixBuilderScoreQueue {
	private ScoreQueue queue;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		queue = new ScoreQueue();
	}

	@Test
	public void testEmptyQueue() {
		assertTrue(queue.isEmpty());
		assertEquals(0, queue.size());
	}
}

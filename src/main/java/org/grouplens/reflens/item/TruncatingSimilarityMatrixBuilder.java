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
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.grouplens.reflens.item.params.NeighborhoodSize;
import org.grouplens.reflens.util.IndexedItemScore;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class TruncatingSimilarityMatrixBuilder implements SimilarityMatrixBuilder {
	private static class Score implements IndexedItemScore, Comparable<Score> {
		private final int index;
		private final float score;
		
		public Score(int i, float s) {
			index = i;
			score = s;
		}
		
		public int getIndex() {
			return index;
		}
		
		public float getScore() {
			return score;
		}
		
		public int compareTo(Score other) {
			return Float.compare(score, other.score);
		}
	}
	
	/* We have to extend the fastutil priority queue to implement Iterable. The
	 * java.util PriorityQueue doesn't implement trim, which we want.  Also,
	 * implementing it ourselves lets us work around lack of generic type
	 * covariance.
	 */
	private static class ScoreQueue extends ObjectHeapPriorityQueue<Object> implements Iterable<IndexedItemScore> {

		@Override
		public Iterator<IndexedItemScore> iterator() {
			return new Iterator<IndexedItemScore>() {
				private int pos = 0;

				@Override
				public boolean hasNext() {
					return pos < size;
				}

				@Override
				public IndexedItemScore next() {
					if (pos < size) {
						Score s = (Score) heap[pos];
						pos += 1;
						return s;
					} else {
						throw new NoSuchElementException();
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
		
	}
	
	private static class Matrix implements SimilarityMatrix, Serializable {
		private static final long serialVersionUID = 6721870011265541987L;
		private ScoreQueue[] rows;
		public Matrix(ScoreQueue[] rows) {
			this.rows = rows;
		}
		@Override
		public Iterable<IndexedItemScore> getNeighbors(int i) {
			return rows[i];
		}
		@Override
		public int size() {
			return rows.length;
		}
	}
	
	private ScoreQueue[] rows;
	private final int maxNeighbors;
	private final int itemCount;
	
	@Inject
	public TruncatingSimilarityMatrixBuilder(
			@NeighborhoodSize int neighborhoodSize,
			@Assisted int nitems) {
		maxNeighbors = neighborhoodSize;
		this.itemCount = nitems;
		setup();
	}
	
	private void setup() {
		rows = new ScoreQueue[itemCount];
		for (int i = 0; i < itemCount; i++) {
			rows[i] = new ScoreQueue();
		}
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.util.SimilarityMatrix#finish()
	 */
	@Override
	public SimilarityMatrix build() {
		for (int i = 0; i < rows.length; i++) {
			rows[i].trim();
		}
		Matrix m = new Matrix(rows);
		rows = null;
		return m;
	}

	/* (non-Javadoc)
	 * @see org.grouplens.reflens.util.SimilarityMatrix#put(int, int, float)
	 */
	@Override
	public void put(int i1, int i2, float sim) {
		if (i2 < 0 || i2 >= rows.length)
			throw new IndexOutOfBoundsException();
		// concurrent read-only array access permitted
		ScoreQueue q = rows[i1];
		// synchronize on this row to add item
		synchronized (q) {
			q.enqueue(new Score(i2, sim));
			while (q.size() > maxNeighbors)
				q.dequeue();
		}
	}
	
	@Override
	public int size() {
		return itemCount;
	}
}

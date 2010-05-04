/**
 * 
 */
package org.grouplens.reflens.bench;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.grouplens.reflens.data.RatingVector;

/**
 * Implementation of a train/test ratings set. It takes care of partitioning the
 * data set into N portions so that each one can be tested against the others.
 * Portions are divided equally, and data is randomized before being
 * partitioned.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
class RatingSet<U,I> {
	private static Logger logger = Logger.getLogger(RatingSet.class.getName());
	private ArrayList<Collection<RatingVector<U,I>>> chunks;

	/**
	 * Construct a new train/test ratings set.
	 * @param folds The number of portions to divide the data set into.
	 * @param ratings The ratings data to partition.
	 */
	public RatingSet(int folds, List<RatingVector<U,I>> ratings) {
		logger.info(String.format("Creating rating set with %d folds", folds));
		chunks = new ArrayList<Collection<RatingVector<U,I>>>(folds);
		
		int chunkSize = ratings.size() / folds + 1;
		for (int i = 0; i < folds; i++) {
			chunks.add(new ArrayList<RatingVector<U,I>>(chunkSize));
		}	
		
		Collections.shuffle(ratings);
		int chunk = 0;
		for (RatingVector<U,I> user: ratings) {
			chunks.get(chunk % folds).add(user);
			chunk++;
		}
	}
	
	public int getChunkCount() {
		return chunks.size();
	}
	
	/**
	 * Build a training data collection.  The collection is built on-demand, so
	 * it doesn't use much excess memory.
	 * @param testIndex The index of the test set to use.
	 * @return The union of all data partitions except testIndex.
	 */
	public Collection<RatingVector<U,I>> trainingSet(int testIndex) {
		return new TrainCollection(testIndex);
	}
	
	/**
	 * Return a test data set.
	 * @param testIndex The index of the test set to use.
	 * @return The test set of users.
	 */
	public Collection<RatingVector<U,I>> testSet(int testIndex) {
		return chunks.get(testIndex);
	}
	
	private class TrainCollection extends AbstractCollection<RatingVector<U,I>> {
		private int size;
		private int testSetIndex;
		
		public TrainCollection(int testSet) {
			testSetIndex = testSet;
			size = 0;
			for (int i = 0; i < chunks.size(); i++) {
				if (i == testSet) continue;
				size += chunks.get(i).size();
			}
		}

		@Override
		public Iterator<RatingVector<U, I>> iterator() {
			return new TrainIterator(testSetIndex);
		}

		@Override
		public int size() {
			return size;
		}
	}
	
	/**
	 * Iterator for iterating over several pieces of the training set.
	 * 
	 * Invariant: baseIter is null or has more items.  currentChunk is the index
	 * of the chunk for which baseIter is an iterator, and is equal to
	 * chunks.size() when we're past the end (and thus baseIter is null).
	 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
	 *
	 */
	private class TrainIterator implements Iterator<RatingVector<U,I>> {
		private int testSetIndex;
		private int currentChunk;
		private Iterator<RatingVector<U,I>> baseIter;
		
		public TrainIterator(int testSet) {
			testSetIndex = testSet;
			currentChunk = 0;
			// set up the first iterator
			while (currentChunk < chunks.size() && baseIter == null) {
				if (currentChunk == testSetIndex) {
					currentChunk++;
					continue;
				}
				baseIter = chunks.get(currentChunk).iterator();
				if (!baseIter.hasNext()) {
					currentChunk++;
					baseIter = null;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return baseIter != null && baseIter.hasNext();
		}

		@Override
		public RatingVector<U, I> next() {
			if (baseIter != null && baseIter.hasNext()) {
				RatingVector<U,I> h = baseIter.next();
				while (baseIter != null && !baseIter.hasNext()) {
					// we need to advance the underlying iterator
					currentChunk++;
					if (currentChunk == testSetIndex) currentChunk++;
					if (currentChunk < chunks.size()) {
						baseIter = chunks.get(currentChunk).iterator();
					} else {
						baseIter = null;
					}
				}
				return h;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
}

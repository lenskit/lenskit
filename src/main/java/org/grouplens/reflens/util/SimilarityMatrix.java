/*
 * RefLens, a reference implementation of recommender algorithms. Copyright 2010
 * Michael Ekstrand <ekstrand@cs.umn.edu>
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

package org.grouplens.reflens.util;


/**
 * Interface for similarity matrices for collaborative filtering.
 * 
 * This interface uses numeric indexes for everything. The indexes are expected
 * to be contiguous starting with 0. Similarity matrices will usually have a
 * fixed size that cannot be exceeded.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface SimilarityMatrix {
	/**
	 * The number of entities for which this matrix can store similarities.
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Store the similarity between two entities.  This is thread-safe until
	 * {@link #finish()} has been called, at which point it cannot be called
	 * at all.  Only one similary should be stored for each ordered pair of
	 * entities, as the implementation may not de-duplicate the matrix.
	 * 
	 * @param i1
	 *            The index of the first entity.
	 * @param i2
	 *            The index of the second entity.
	 * @param sim
	 *            The similarity score.
	 * @throws IndexOutOfBoundsException
	 *             if an index is out of bounds (<0 or >{@link #size()}).
	 * @throws RuntimeException
	 *             may be thrown if {@link #finish()} has been called. Throwing
	 *             this exception is optional; some implementations may not
	 *             check and throw it.
	 */
	public void put(int i1, int i2, float sim);

	/**
	 * Retrieve the neighbors for an item.
	 * 
	 * @param i
	 *            All neighbors for item <tt>i</tt>. This is all similarity
	 *            scores for which <tt>i</tt> was passed as the first argument
	 *            to {@link #put(int, int, float)}.
	 * @return A map of neighbors to similarity scores.
	 * @throws IndexOutOfBoundsException
	 *             if the index is invalid.
	 * @throws RuntimeException
	 *             may be thrown if {@link #finish()} has not been called.
	 */
	public Iterable<IndexedItemScore> getNeighbors(int i);

	/**
	 * Finalize the matrix after all entries have been added. It is an error to
	 * call {@link #put(int, int, float)} after calling this method.
	 */
	public void finish();
}

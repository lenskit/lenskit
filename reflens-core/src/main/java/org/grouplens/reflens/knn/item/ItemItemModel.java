/*
 * RefLens, a reference implementation of recommender algorithms.
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

package org.grouplens.reflens.knn.item;

import it.unimi.dsi.fastutil.objects.ObjectCollections;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.MutableSparseVector;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.data.SparseVector;
import org.grouplens.reflens.knn.SimilarityMatrix;
import org.grouplens.reflens.util.IndexedItemScore;

/**
 * Encapsulation of the model needed for item-item collaborative filtering.
 * 
 * This class is used by {@link ItemItemRecommenderService} to do actual item-item
 * recommendation.  It encapsulates the various data accesses needed to support
 * item-item CF.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
@Immutable
public class ItemItemModel implements Serializable {

	private static final long serialVersionUID = 7040201805529926395L;
	
	private final Index itemIndexer;
	private final SimilarityMatrix matrix;
	private final RatingPredictor baseline;
	
	public ItemItemModel(Index indexer, RatingPredictor baseline, SimilarityMatrix matrix) {
		this.itemIndexer = indexer;
		this.baseline = baseline;
		this.matrix = matrix;
	}
	
	public Iterable<IndexedItemScore> getNeighbors(long item) {
		int idx = itemIndexer.getIndex(item);
		if (idx >= 0) {
			return matrix.getNeighbors(itemIndexer.getIndex(item));
		} else {
			return new ObjectCollections.EmptyCollection<IndexedItemScore>() {};
		}
	}
	
	public int getItemIndex(long id) {
		return itemIndexer.getIndex(id);
	}
	
	public long getItem(int idx) {
		return itemIndexer.getId(idx);
	}

	/**
	 * Subtract the baseline recommender from a set of ratings.
	 * <p>
	 * This method computes the baseline predictions for all items in <var>target</var>
	 * and subtracts the prediction from the value in <var>target</var>.  This
	 * subtraction is done in-place by calling {@link MutableSparseVector#subtract(MutableSparseVector)}
	 * on <var>target</var>.
	 * 
	 * @param user The user ID.
	 * @param ratings The user's rating vector.
	 * @param target The vector from which the baseline is to be subtracted.
	 */
	public void subtractBaseline(long user, SparseVector ratings, MutableSparseVector target) {
		if (baseline != null) {
			SparseVector basePreds = baseline.predict(user, ratings, target.idSet());
			target.subtract(basePreds);
		}
	}
	
	public void addBaseline(long user, SparseVector ratings, MutableSparseVector target) {
		SparseVector basePreds = baseline.predict(user, ratings, target.idSet());
		target.add(basePreds);
	}
	
	public double addBaseline(long user, SparseVector ratings, long item, double prediction) {
		if (baseline != null) {
			ScoredId basePred = baseline.predict(user, ratings, item);
			prediction += basePred.getScore();
		}
		return prediction;
	}
}
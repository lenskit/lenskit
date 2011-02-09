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

package org.grouplens.reflens.knn;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectCollections;

import java.io.Serializable;

import javax.annotation.concurrent.Immutable;

import org.grouplens.reflens.RatingPredictor;
import org.grouplens.reflens.data.Index;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.ScoredId;
import org.grouplens.reflens.util.IndexedItemScore;
import org.grouplens.reflens.util.SimilarityMatrix;

/**
 * Encapsulation of the model needed for item-item collaborative filtering.
 * 
 * This class is used by {@link ItemItemRecommender} to do actual item-item
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

	public RatingVector subtractBaseline(long user, RatingVector ratings) {
		if (baseline != null) {
			RatingVector basePreds = baseline.predict(user, ratings, ratings.idSet());
			RatingVector normed = new RatingVector(ratings.size());
			for (Long2DoubleMap.Entry e: ratings.fast()) {
				normed.put(e.getLongKey(), e.getDoubleValue() - basePreds.get(e.getKey()));
			}
			return normed;
		} else {
			return ratings;
		}
	}
	
	public RatingVector addBaseline(long user, RatingVector ratings, RatingVector predictions) {
		RatingVector basePreds = baseline.predict(user, ratings, predictions.idSet());
		RatingVector normed = new RatingVector(predictions.size());
		for (Long2DoubleMap.Entry e: predictions.fast()) {
			normed.put(e.getLongKey(), e.getDoubleValue() + basePreds.get(e.getKey()));
		}
		return normed;
	}
	
	public double addBaseline(long user, RatingVector ratings, long item, double prediction) {
		if (baseline != null) {
			ScoredId basePred = baseline.predict(user, ratings, item);
			prediction += basePred.getScore();
		}
		return prediction;
	}
}
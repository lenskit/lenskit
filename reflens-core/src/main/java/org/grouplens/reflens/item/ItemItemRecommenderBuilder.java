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
 */

package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Cursor;
import org.grouplens.reflens.data.DataSet;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;

import com.google.inject.Inject;

public class ItemItemRecommenderBuilder implements RecommenderBuilder {
	
	private SimilarityMatrixBuilderFactory matrixFactory;
	private Normalizer<Long, Map<Long,Double>> ratingNormalizer;
	private Similarity<? super Long2DoubleMap> itemSimilarity;

	@Inject
	ItemItemRecommenderBuilder(
			SimilarityMatrixBuilderFactory matrixFactory,
			@ItemSimilarity Similarity<? super Long2DoubleMap> itemSimilarity,
			@Nullable @RatingNormalization Normalizer<Long,Map<Long,Double>> ratingNormalizer) {
		this.matrixFactory = matrixFactory;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
	}
	
	@Override
	public ItemItemRecommender build(DataSet<UserRatingProfile> data) {
		Indexer indexer = new Indexer();
		List<Long2DoubleMap> itemRatings = buildItemRatings(indexer, data);
		
		// prepare the similarity matrix
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.size());
		
		// compute the similarity matrix
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			// we can compute equivalent symmetries at the same time
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = i+1; j < itemRatings.size(); j++) {
					double sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0) {
						builder.put(i, j, sim);
						builder.put(j, i, sim);
					}
				}
			}
		} else {
			// less efficient route
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = 0; j < itemRatings.size(); j++) {
					double sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0)
						builder.put(i, j, sim);
				}
			}
		}
		
		SimilarityMatrix matrix = builder.build();
		ItemItemModel model = new ItemItemModel(indexer, matrix);
		return new ItemItemRecommender(model);
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return
	 */
	private List<Long2DoubleMap> buildItemRatings(Indexer indexer, DataSet<UserRatingProfile> data) {
		ArrayList<Long2DoubleMap> itemVectors = new ArrayList<Long2DoubleMap>();
		Cursor<UserRatingProfile> cursor = data.cursor();
		try {
			for (UserRatingProfile user: cursor) {
				Map<Long,Double> ratings = user.getRatings();
				if (ratingNormalizer != null)
					ratings = ratingNormalizer.normalize(user.getUser(), ratings);
				for (Map.Entry<Long, Double> rating: ratings.entrySet()) {
					long item = rating.getKey();
					int idx = indexer.internId(item);
					if (idx >= itemVectors.size()) {
						// it's a new item - add one
						assert idx == itemVectors.size();
						itemVectors.add(new Long2DoubleOpenHashMap());
					}
					itemVectors.get(idx).put(user.getUser(), (double) rating.getValue());
				}
			}
		} finally {
			cursor.close();
		}
		itemVectors.trimToSize();
		return itemVectors;
	}
}

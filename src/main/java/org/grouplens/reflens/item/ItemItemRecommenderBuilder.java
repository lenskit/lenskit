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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.UserRatingProfile;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ItemItemRecommenderBuilder<U,I> implements RecommenderBuilder<U, I> {
	
	private Provider<Indexer<I>> indexProvider;
	private Provider<Map<U,Float>> itemMapProvider;
	private SimilarityMatrixBuilderFactory matrixFactory;
	private Normalizer<U, Map<I,Float>> ratingNormalizer;
	private Similarity<Map<U,Float>> itemSimilarity;

	@Inject
	ItemItemRecommenderBuilder(
			Provider<Indexer<I>> indexProvider,
			Provider<Map<U,Float>> itemMapProvider,
			SimilarityMatrixBuilderFactory matrixFactory,
			@ItemSimilarity Similarity<Map<U,Float>> itemSimilarity,
			@Nullable @RatingNormalization Normalizer<U,Map<I,Float>> ratingNormalizer) {
		this.indexProvider = indexProvider;
		this.itemMapProvider = itemMapProvider;
		this.matrixFactory = matrixFactory;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
	}
	
	@Override
	public ItemItemRecommender<U,I> build(Collection<UserRatingProfile<U,I>> data) {
		Indexer<I> indexer = indexProvider.get();
		List<Map<U,Float>> itemRatings = buildItemRatings(indexer, data);
		
		// prepare the similarity matrix
		SimilarityMatrixBuilder builder = matrixFactory.create(itemRatings.size());
		
		// compute the similarity matrix
		if (itemSimilarity instanceof SymmetricBinaryFunction) {
			// we can compute equivalent symmetries at the same time
			for (int i = 0; i < itemRatings.size(); i++) {
				for (int j = i+1; j < itemRatings.size(); j++) {
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
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
					float sim = itemSimilarity.similarity(itemRatings.get(i), itemRatings.get(j));
					if (sim > 0.0)
						builder.put(i, j, sim);
				}
			}
		}
		
		SimilarityMatrix matrix = builder.build();
		ItemItemModel<U,I> model = new ItemItemModel<U,I>(indexer, matrix);
		return new ItemItemRecommender<U,I>(model);
	}
	
	/** 
	 * Transpose the ratings matrix so we have a list of item rating vectors.
	 * @return
	 */
	private List<Map<U,Float>> buildItemRatings(Indexer<I> indexer, Collection<UserRatingProfile<U,I>> data) {
		ArrayList<Map<U,Float>> itemVectors = new ArrayList<Map<U,Float>>();
		for (UserRatingProfile<U,I> user: data) {
			Map<I,Float> ratings = user.getRatings();
			if (ratingNormalizer != null)
				ratings = ratingNormalizer.normalize(user.getUser(), ratings);
			for (Map.Entry<I, Float> rating: ratings.entrySet()) {
				I item = rating.getKey();
				int idx = indexer.internObject(item);
				if (idx >= itemVectors.size()) {
					// it's a new item - add one
					assert idx == itemVectors.size();
					itemVectors.add(itemMapProvider.get());
				}
				itemVectors.get(idx).put(user.getUser(), rating.getValue());
			}
		}
		itemVectors.trimToSize();
		return itemVectors;
	}
}

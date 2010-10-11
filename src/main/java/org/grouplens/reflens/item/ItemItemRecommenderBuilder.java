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

import javax.annotation.Nullable;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.SymmetricBinaryFunction;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.ObjectValue;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.RatingVectorFactory;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.SimilarityMatrix;
import org.grouplens.reflens.util.SimilarityMatrixBuilder;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ItemItemRecommenderBuilder<U,I> implements RecommenderBuilder<U, I> {
	
	private Provider<Indexer<I>> indexProvider;
	private RatingVectorFactory<I, U> itemVectorFactory;
	private SimilarityMatrixBuilderFactory matrixFactory;
	private Normalizer<RatingVector<U, I>> ratingNormalizer;
	private Similarity<RatingVector<I, U>> itemSimilarity;

	@Inject
	ItemItemRecommenderBuilder(
			Provider<Indexer<I>> indexProvider,
			RatingVectorFactory<I, U> itemVectorFactory,
			SimilarityMatrixBuilderFactory matrixFactory,
			@ItemSimilarity Similarity<RatingVector<I,U>> itemSimilarity,
			@Nullable @RatingNormalization Normalizer<RatingVector<U,I>> ratingNormalizer) {
		this.indexProvider = indexProvider;
		this.itemVectorFactory = itemVectorFactory;
		this.matrixFactory = matrixFactory;
		this.ratingNormalizer = ratingNormalizer;
		this.itemSimilarity = itemSimilarity;
	}
	
	@Override
	public ItemItemRecommender<U,I> build(Collection<RatingVector<U,I>> data) {
		Indexer<I> indexer = indexProvider.get();
		List<RatingVector<I,U>> itemRatings = buildItemRatings(indexer, data);
		
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
	private List<RatingVector<I,U>> buildItemRatings(Indexer<I> indexer, Collection<RatingVector<U,I>> ratings) {
		ArrayList<RatingVector<I,U>> itemVectors = new ArrayList<RatingVector<I,U>>();
		for (RatingVector<U,I> user: ratings) {
			if (ratingNormalizer != null)
				user = ratingNormalizer.normalize(user);
			for (ObjectValue<I> rating: user) {
				I item = rating.getItem();
				int idx = indexer.internObject(item);
				if (idx >= itemVectors.size()) {
					// it's a new item - add one
					assert idx == itemVectors.size();
					itemVectors.add(itemVectorFactory.make(item));
				}
				itemVectors.get(idx).putRating(user.getOwner(), rating.getRating());
			}
		}
		itemVectors.trimToSize();
		return itemVectors;
	}
}

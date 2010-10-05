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

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.NeighborhoodSize;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.name.Names;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemRecommenderModule extends AbstractModule {

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(new TypeLiteral<Similarity<RatingVector<Integer, Integer>>>() {}).annotatedWith(ItemSimilarity.class).to(new TypeLiteral<CosineSimilarity<Integer, RatingVector<Integer,Integer>>>() {});
		bind(new TypeLiteral<Normalization<RatingVector<Integer,Integer>>>() {}).annotatedWith(RatingNormalization.class).to(new TypeLiteral<MeanNormalization<Integer, Integer>>(){});
		bind(int.class).annotatedWith(NeighborhoodSize.class).toInstance(100);
		bind(SimilarityMatrixBuilderFactory.class).toProvider(
				FactoryProvider.newFactory(SimilarityMatrixBuilderFactory.class, PQueueSimilarityMatrixBuilder.class));
		bind(new TypeLiteral<RecommenderFactory<Integer, Integer>>() {}).to(new TypeLiteral<ItemBasedRecommenderFactory<Integer,Integer>>() {});
	}
}

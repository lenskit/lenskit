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

import java.lang.reflect.Type;
import java.util.Properties;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.NeighborhoodSize;
import org.grouplens.reflens.item.params.RatingNormalization;
import org.grouplens.reflens.util.ObjectLoader;
import org.grouplens.reflens.util.SimilarityMatrixBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.util.Providers;
import com.google.inject.util.Types;

/**
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class ItemRecommenderModule extends AbstractModule {
	
	private static final Logger logger = LoggerFactory.getLogger(ItemRecommenderModule.class);
	
	private Properties properties;
	
	public ItemRecommenderModule() {
		this(System.getProperties());
	}

	public ItemRecommenderModule(Properties props) {
		this.properties = props;
	}
	
	protected Type userType() {
		return Integer.class;
	}
	
	protected Type itemType() {
		return Integer.class;
	}

	/* (non-Javadoc)
	 * @see com.google.inject.AbstractModule#configure()
	 */
	@Override
	protected void configure() {
		bind(new TypeLiteral<Similarity<RatingVector<Integer, Integer>>>() {}).annotatedWith(ItemSimilarity.class).to(new TypeLiteral<CosineSimilarity<Integer, RatingVector<Integer,Integer>>>() {});
		
		String rnorm = properties.getProperty(RatingNormalization.PROPERTY_NAME);
		Type rvType = Types.newParameterizedType(RatingVector.class, userType(), itemType());
		Type rnType = Types.newParameterizedType(Normalizer.class, rvType);
		Key rnKey = Key.get(rnType, RatingNormalization.class);
		logger.debug("Using rating norm key {}", rnKey.toString());
		if (rnorm != null) {
			logger.info("Using rating normalizer {}", rnorm);
			Class rnclass = ObjectLoader.getClass(rnorm);
			bind(rnKey).to(rnclass);
		} else {
			logger.debug("Using no rating normalizer");
			bind(rnKey).toProvider(Providers.of(null));
		}
		bind(int.class).annotatedWith(NeighborhoodSize.class).toInstance(
				Integer.parseInt(properties.getProperty(NeighborhoodSize.PROPERTY_NAME, "100"), 10));

		bind(SimilarityMatrixBuilderFactory.class).toProvider(
				FactoryProvider.newFactory(SimilarityMatrixBuilderFactory.class, TruncatingSimilarityMatrixBuilder.class));
		bind(new TypeLiteral<RecommenderBuilder<Integer, Integer>>() {}).to(new TypeLiteral<ItemItemRecommenderBuilder<Integer,Integer>>() {});
	}
}

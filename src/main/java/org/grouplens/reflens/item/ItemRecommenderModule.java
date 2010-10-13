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
import java.util.Map;
import java.util.Properties;

import org.grouplens.reflens.Normalizer;
import org.grouplens.reflens.RecommenderBuilder;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.UserRatingProfile;
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
		configureSimilarityMatrix();
		
		configureUserNormalizer();
		configureNeighborhoodSize();

		configureItemSimilarity();
		configureRecommenderBuilder();
	}

	/**
	 * 
	 */
	private void configureRecommenderBuilder() {
		bind(new TypeLiteral<RecommenderBuilder<Integer, Integer>>() {}).to(new TypeLiteral<ItemItemRecommenderBuilder<Integer,Integer>>() {});
	}

	/**
	 * 
	 */
	protected void configureSimilarityMatrix() {
		bind(SimilarityMatrixBuilderFactory.class).toProvider(
				FactoryProvider.newFactory(SimilarityMatrixBuilderFactory.class, TruncatingSimilarityMatrixBuilder.class));
	}

	/**
	 * 
	 */
	protected void configureNeighborhoodSize() {
		bind(int.class).annotatedWith(NeighborhoodSize.class).toInstance(
				Integer.parseInt(properties.getProperty(NeighborhoodSize.PROPERTY_NAME, "100"), 10));
	}

	/**
	 * 
	 */
	protected void configureUserNormalizer() {
		Type rvType = Types.newParameterizedType(UserRatingProfile.class, userType(), itemType());
		Type rnType = Types.newParameterizedType(Normalizer.class, rvType);
		Key rnKey = Key.get(rnType, RatingNormalization.class);
		logger.debug("Using rating norm key {}", rnKey.toString());
		bindClassFromProperty(rnKey, RatingNormalization.PROPERTY_NAME);
	}
	
	protected void bindClassFromProperty(Key key, String propName) {
		bindClassFromProperty(key, propName, null);
	}

	/**
	 * @param propName
	 * @param key
	 */
	protected void bindClassFromProperty(Key key, String propName, Class dftClass) {
		String rnorm = properties.getProperty(propName);
		Class target = dftClass;
		if (rnorm != null) {
			logger.debug("Binding {} to {}", key.toString(), propName);
			target = ObjectLoader.getClass(rnorm);
		}
		
		if (target != null) {
			bind(key).to(target);
		} else {
			logger.debug("Binding {} to null", key.toString());
			bind(key).toProvider(Providers.of(null));
		}
	}
	
	protected void configureItemSimilarity() {
		bind(new TypeLiteral<Similarity<Map<Integer, Float>>>() {}).annotatedWith(ItemSimilarity.class).to(new TypeLiteral<CosineSimilarity<Integer>>() {});
	}
}

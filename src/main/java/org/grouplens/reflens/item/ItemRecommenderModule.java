/**
 * 
 */
package org.grouplens.reflens.item;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.item.params.ItemSimilarity;
import org.grouplens.reflens.item.params.NeighborhoodSize;
import org.grouplens.reflens.item.params.RatingNormalization;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
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
		
		bind(new TypeLiteral<RecommenderFactory<Integer, Integer>>() {}).to(new TypeLiteral<ItemBasedRecommenderFactory<Integer,Integer>>() {});
	}
}

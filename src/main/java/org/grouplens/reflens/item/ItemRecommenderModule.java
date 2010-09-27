/**
 * 
 */
package org.grouplens.reflens.item;

import it.unimi.dsi.fastutil.ints.Int2FloatMap;

import org.grouplens.reflens.Normalization;
import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.Similarity;
import org.grouplens.reflens.data.Indexer;
import org.grouplens.reflens.data.RatingVector;
import org.grouplens.reflens.data.RatingVectorFactory;
import org.grouplens.reflens.data.integer.IntIndexer;
import org.grouplens.reflens.data.integer.IntRatingVectorFactory;

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
		bind(new TypeLiteral<RatingVectorFactory<Integer,Integer>>() {}).to(new TypeLiteral<IntRatingVectorFactory<Integer>>() {});
		bind(new TypeLiteral<Indexer<Integer>>() {}).to(IntIndexer.class);
		bind(new TypeLiteral<Similarity<RatingVector<Integer, Integer>>>() {}).annotatedWith(Names.named("ItemSim")).to(new TypeLiteral<CosineSimilarity<Integer, RatingVector<Integer,Integer>>>() {});
		bind(new TypeLiteral<Normalization<RatingVector<Integer,Integer>>>() {}).annotatedWith(Names.named("RatingNorm")).to(new TypeLiteral<MeanNormalization<Integer, Integer>>(){});
		bind(int.class).annotatedWith(Names.named("NeighborhoodSize")).toInstance(100);
		
		bind(new TypeLiteral<RecommenderFactory<Integer, Integer>>() {}).to(new TypeLiteral<ItemBasedRecommenderFactory<Integer,Integer>>() {});
	}
}

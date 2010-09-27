package org.grouplens.reflens.svd;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import java.util.Map;

import org.grouplens.reflens.RecommenderFactory;
import org.grouplens.reflens.svd.params.FeatureCount;
import org.grouplens.reflens.svd.params.LearningRate;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class FunkSVDModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(int.class).annotatedWith(FeatureCount.class).toInstance(50);
		bind(float.class).annotatedWith(LearningRate.class).toInstance(0.001f);
		bind(new TypeLiteral<RecommenderFactory<Integer,Integer>>(){}).to(new TypeLiteral<FunkSVDFactory<Integer,Integer>>(){});
	}

}

package org.grouplens.lenskit.util.fbSamples;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public class SampleInnerComponentBuilder {
	
	public static class ComponentBuilder implements RecommenderComponentBuilder<SampleComponent> {
		
		public SampleComponent build(RatingBuildContext someContext) {
			return new SampleComponent();
		}
	}
	
	public class ComponentBuilder2 implements RecommenderComponentBuilder<SampleComponentNoBuilder> {
		
		public SampleComponentNoBuilder build(RatingBuildContext someContext) {
			return new SampleComponentNoBuilder();
		}
	}
}

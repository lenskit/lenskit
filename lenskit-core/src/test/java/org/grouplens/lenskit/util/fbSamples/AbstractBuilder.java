package org.grouplens.lenskit.util.fbSamples;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public abstract class AbstractBuilder implements RecommenderComponentBuilder<SampleComponentNoBuilder> {
	
	public SampleComponentNoBuilder build(RatingBuildContext someContext) {
		return new SampleComponentNoBuilder();
	}
}
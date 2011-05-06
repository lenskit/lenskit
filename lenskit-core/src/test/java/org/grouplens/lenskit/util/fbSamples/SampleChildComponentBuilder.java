package org.grouplens.lenskit.util.fbSamples;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public class SampleChildComponentBuilder implements RecommenderComponentBuilder<SampleChildComponent> {
	
	public SampleChildComponent build(RatingBuildContext someContext) {
		return new SampleChildComponent();
	}

}
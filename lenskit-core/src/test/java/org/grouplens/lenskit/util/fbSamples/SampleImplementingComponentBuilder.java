package org.grouplens.lenskit.util.fbSamples;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public class SampleImplementingComponentBuilder implements RecommenderComponentBuilder<SampleImplementingComponent> {

	public SampleImplementingComponent build(RatingBuildContext someContext) {
		return new SampleImplementingComponent();
	}

}

package org.grouplens.lenskit.util.fbSamples;

import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public class SampleComponent2Builder implements RecommenderComponentBuilder<SampleComponent2> {
	
	public SampleComponent2 build(RatingBuildContext someContext) {
		return new SampleComponent2();
	}
}

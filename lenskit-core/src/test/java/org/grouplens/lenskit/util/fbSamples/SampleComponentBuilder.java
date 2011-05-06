package org.grouplens.lenskit.util.fbSamples;
import org.grouplens.lenskit.RecommenderComponentBuilder;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public class SampleComponentBuilder implements RecommenderComponentBuilder<SampleComponent> {
	
	public SampleComponent build(RatingBuildContext someContext) {
		return new SampleComponent();
	}

}

package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.context.RatingBuildContext;

public interface SVDModelBuilder {

    public abstract SVDModel build(RatingBuildContext data,
            RatingPredictor baseline);

}
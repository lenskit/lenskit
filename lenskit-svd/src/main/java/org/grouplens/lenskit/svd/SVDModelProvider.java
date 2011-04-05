package org.grouplens.lenskit.svd;

import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.data.context.RatingBuildContext;
import org.grouplens.lenskit.params.BaselinePredictor;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provider that uses an {@link SVDModelBuilder} to build an {@link SVDModel}.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SVDModelProvider implements Provider<SVDModel> {
    private final SVDModelBuilder builder;
    private final RatingBuildContext buildContext;
    private final RatingPredictor baseline;
    
    @Inject
    public SVDModelProvider(SVDModelBuilder builder, RatingBuildContext ctx,
            @BaselinePredictor RatingPredictor base) {
        this.builder = builder;
        buildContext = ctx;
        baseline = base;
    }

    @Override
    public SVDModel get() {
        return builder.build(buildContext, baseline);
    }

}

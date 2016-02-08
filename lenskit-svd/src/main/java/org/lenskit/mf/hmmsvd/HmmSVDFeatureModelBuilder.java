package org.lenskit.mf.hmmsvd;

import java.io.IOException;

import org.lenskit.solver.objective.ObjectiveFunction;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class HmmSVDFeatureModelBuilder {
    private HmmSVDFeatureModel model;

    public HmmSVDFeatureModelBuilder() {
        model = new HmmSVDFeatureModel(numBiases, numFactors, factDim, dao);
    }

    public HmmSVDFeatureModel build() throws IOException {
        model.assignVariables();
        return model;
    }
}

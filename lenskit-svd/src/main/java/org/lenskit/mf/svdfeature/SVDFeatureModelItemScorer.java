package org.grouplens.lenskit.mf.svdfeature;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class SVDFeatureModelItemScorer {
    private SVDFeatureModel model;

    public SVDFeatureModelItemScorer(SVDFeatureModel inModel) {
        model = inModel;
    }

    public double predict(SVDFeatureInstance ins) {
        return model.predict(ins, false);
    }
}

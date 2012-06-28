package org.grouplens.lenskit.transform.threshold;

import org.grouplens.lenskit.params.ThresholdValue;

import javax.inject.Inject;

/**
 * Checks similarity values to ensure their real values are
 * over the {@link ThresholdValue}.
 */
public class RealThreshold implements Threshold {

    private final double thresholdValue;

    @Inject
    public RealThreshold(@ThresholdValue double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public boolean retain(double sim) {
        return sim > thresholdValue;
    }
}

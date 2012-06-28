package org.grouplens.lenskit.transform.threshold;

import org.grouplens.lenskit.params.ThresholdValue;

import javax.inject.Inject;

/**
 * Checks similarity values to ensure their absolute values are
 * over the {@link ThresholdValue}.
 */
public class AbsoluteThreshold implements Threshold {

    private final double thresholdValue;

    @Inject
    public AbsoluteThreshold(@ThresholdValue double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public boolean retain(double sim) {
        return Math.abs(sim) > thresholdValue;
    }
}

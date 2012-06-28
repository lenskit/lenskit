package org.grouplens.lenskit.transform.threshold;

import org.grouplens.grapht.annotation.DefaultImplementation;

/**
 * Determine whether similarity values should be accepted into
 * or rejected from similarity models.
 */
@DefaultImplementation(RealThreshold.class)
public interface Threshold {

    /**
     * Checks a similarity value against retention criteria for
     * inclusion in similarity models.
     * @param sim The double similarity value to check against
     *        the threshold.
     * @return true if the parameter similarity value should be
     *         retained in the similarity model, false otherwise.
     */
    abstract boolean retain(double sim);

}

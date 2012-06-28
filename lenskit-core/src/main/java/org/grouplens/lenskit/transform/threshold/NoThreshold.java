package org.grouplens.lenskit.transform.threshold;

/**
 * Represents the absence of a threshold function, choosing
 * to retain all similarity values passed to it.
 */
public class NoThreshold implements Threshold {

    public boolean retain(double sim) {
        return true;
    }
}

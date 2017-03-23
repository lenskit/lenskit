package org.lenskit.mf.BPR;

/**
 * Class that operates over training events and generates pairs of objects with a defined preference.
 */
public interface TrainingPairGenerator {
    /**
     * @return a new training pair. For now this should loop infinitely, however in the long term this should be a bit smarter.
     */
    TrainingItemPair nextPair();
}

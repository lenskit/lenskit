package org.grouplens.lenskit.mf.funksvd;

import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.collections.FastCollection;
import org.grouplens.lenskit.data.pref.IndexedPreference;
import org.grouplens.lenskit.data.snapshot.PreferenceSnapshot;
import org.grouplens.lenskit.transform.clamp.ClampingFunction;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;

/**
 * Rating estimates used while training the predictor.
 */
final class TrainingEstimator {
    private final FastCollection<IndexedPreference> ratings;
    private final ClampingFunction clamp;
    private final double[] estimates;

    /**
     * Create the set of training data.
     *
     * @param snap     The preference snapshot.
     * @param baseline The baseline predictor.
     * @param cf       The clamping function.
     */
    public TrainingEstimator(PreferenceSnapshot snap, BaselinePredictor baseline, ClampingFunction cf) {
        ratings = snap.getRatings();
        clamp = cf;
        estimates = new double[ratings.size()];

        final LongCollection userIds = snap.getUserIds();
        LongIterator userIter = userIds.iterator();
        while (userIter.hasNext()) {
            long uid = userIter.nextLong();
            SparseVector rvector = snap.userRatingVector(uid);
            MutableSparseVector blpreds = new MutableSparseVector(rvector.keySet());
            baseline.predict(uid, rvector, blpreds);

            for (IndexedPreference r : CollectionUtils.fast(snap.getUserRatings(uid))) {
                estimates[r.getIndex()] = blpreds.get(r.getItemId());
            }
        }
    }

    /**
     * Get the estimate for a preference.
     * @param pref The preference.
     * @return The estimate.
     */
    public double get(IndexedPreference pref) {
        return estimates[pref.getIndex()];
    }

    /**
     * Update the current estimates with trained values for a new feature.
     * @param ufvs The user feature values.
     * @param ifvs The item feature values.
     */
    public void update(double[] ufvs, double[] ifvs) {
        for (IndexedPreference r : CollectionUtils.fast(ratings)) {
            double est = estimates[r.getIndex()];
            double offset = ufvs[r.getUserIndex()] * ifvs[r.getItemIndex()];
            estimates[r.getIndex()] = clamp.apply(r.getUserId(), r.getItemId(), est + offset);
        }
    }
}

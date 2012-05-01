package org.grouplens.lenskit.data.pref;

import it.unimi.dsi.fastutil.longs.Long2DoubleOpenHashMap;
import org.grouplens.lenskit.collections.CollectionUtils;
import org.grouplens.lenskit.vectors.MutableSparseVector;

import java.util.Collection;

/**
 * Utility class for working with preferences.
 * @author Michael Ekstrand
 */
public class Preferences {
    /**
     * Compute a user preference vector.
     * @param prefs The user's preferences.
     * @return A vector of the preferences.
     * @throws IllegalArgumentException if the same item appears multiple times, or there are
     *                                  preferences from multiple users.
     */
    public static MutableSparseVector userPreferenceVector(Collection<? extends Preference> prefs) {
        // find keys and pre-validate data
        Long2DoubleOpenHashMap prefMap = new Long2DoubleOpenHashMap(prefs.size());
        long user = 0;
        for (Preference p: CollectionUtils.fast(prefs)) {
            final long iid = p.getItemId();
            if (prefMap.isEmpty()) {
                user = p.getUserId();
            } else if (user != p.getUserId()) {
                throw new IllegalArgumentException("multiple user IDs in pref array");
            }
            if (prefMap.containsKey(iid)) {
                throw new IllegalArgumentException("item " + iid + " occurs multiple times");
            } else {
                prefMap.put(iid, p.getValue());
            }
        }

        return new MutableSparseVector(prefMap);
    }
}

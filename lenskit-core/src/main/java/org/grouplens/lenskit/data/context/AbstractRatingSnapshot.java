package org.grouplens.lenskit.data.context;

import java.util.IdentityHashMap;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.grouplens.lenskit.norm.NormalizedRatingSnapshot;
import org.grouplens.lenskit.norm.UserRatingVectorNormalizer;

public abstract class AbstractRatingSnapshot implements RatingSnapshot {
    private final IdentityHashMap<UserRatingVectorNormalizer, NormalizedRatingSnapshot>
        normalizedContexts;
    
    public AbstractRatingSnapshot() {
        normalizedContexts = new IdentityHashMap<UserRatingVectorNormalizer, NormalizedRatingSnapshot>();
    }
    
    @Override
    public NormalizedRatingSnapshot normalize(UserRatingVectorNormalizer norm) {
        synchronized (normalizedContexts) {
            NormalizedRatingSnapshot nrbc = normalizedContexts.get(norm);
            if (nrbc == null) {
                nrbc = new NormalizedRatingSnapshot(this, norm);
                normalizedContexts.put(norm, nrbc);
            }
            return nrbc;
        }
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void close() {
        // Help the garbage collector out and clear the cache now
        synchronized (normalizedContexts) {
            normalizedContexts.clear();
        }
    }

}

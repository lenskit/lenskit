/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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

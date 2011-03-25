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
package org.grouplens.lenskit.eval.crossfold;

import org.grouplens.lenskit.data.vector.SparseVector;

/**
 * User profile split into train and test sets.
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public class SplitUserRatingProfile {
    private final long userId;
    private final SparseVector queryVector;
    private final SparseVector probeVector;
    
    public SplitUserRatingProfile(long uid, SparseVector query, SparseVector probe) {
        userId = uid;
        queryVector = query;
        probeVector = probe;
    }
    
    /**
     * Get the user ID.
     * @return
     */
    public long getUserId() {
        return userId;
    }
    
    /**
     * Get the query vector (ratings provided as the user profile).
     * @return
     */
    public SparseVector getQueryVector() {
        return queryVector;
    }
    
    /**
     * Get the probe vector (ratings to try to predict).
     * @return
     */
    public SparseVector getProbeVector() {
        return probeVector;
    }
}

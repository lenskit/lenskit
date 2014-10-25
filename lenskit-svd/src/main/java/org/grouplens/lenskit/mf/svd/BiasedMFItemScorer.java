/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2014 LensKit Contributors.  See CONTRIBUTORS.md.
 * Work on LensKit has been funded by the National Science Foundation under
 * grants IIS 05-34939, 08-08692, 08-12148, and 10-17697.
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
package org.grouplens.lenskit.mf.svd;

import mikera.vectorz.AVector;
import org.grouplens.lenskit.ItemScorer;
import org.grouplens.lenskit.baseline.BaselineScorer;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Item scorer using biased matrix factorization.  This implements SVD-style item scorers.
 *
 * @since 2.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BiasedMFItemScorer extends AbstractItemScorer {
    private final MFModel model;
    private final BiasedMFKernel kernel;
    private final ItemScorer baseline;

    /**
     * Create a new biased MF item scorer.
     * @param mod The model (factorized matrix)
     * @param kern The kernel function to compute scores.
     * @param bl The baseline scorer (used to compute biases).
     */
    @Inject
    public BiasedMFItemScorer(MFModel mod, BiasedMFKernel kern,
                              @BaselineScorer ItemScorer bl) {
        model = mod;
        kernel = kern;
        baseline = bl;
    }

    /**
     * Get a user's preference vector.
     *
     *
     * @param user The user ID.
     * @return The user's preference vector, or {@code null} if no preferences are available for the
     *         user.
     */
    @Nullable
    protected AVector getUserPreferenceVector(long user) {
        return model.getUserVector(user);
    }

    @Override
    public void score(long user, @Nonnull MutableSparseVector scores) {
        baseline.score(user, scores);

        AVector uvec = getUserPreferenceVector(user);
        if (uvec == null) {
            return;
        }

        // scores is now prepopulated with biases, vector is loaded
        for (VectorEntry e: scores) {
            long item = e.getKey();
            AVector ivec = model.getItemVector(item);
            if (ivec != null) {
                scores.set(e, kernel.apply(e.getValue(), uvec, ivec));
            }
        }
    }
}

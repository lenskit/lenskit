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

import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.api.ItemScorer;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.baseline.BaselineScorer;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    protected RealVector getUserPreferenceVector(long user) {
        return model.getUserVector(user);
    }

    public MFModel getModel() {
        return model;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleFunction base = LongUtils.asLong2DoubleFunction(baseline.score(user, items));

        RealVector uvec = getUserPreferenceVector(user);
        if (uvec == null) {
            return Results.newResultMap();
        }

        List<Result> results = new ArrayList<>(items.size());
        LongIterator iter = LongIterators.asLongIterator(items.iterator());
        while (iter.hasNext()) {
            long item = iter.nextLong();
            RealVector ivec = model.getItemVector(item);
            if (ivec != null) {
                double score = kernel.apply(base.get(item), uvec, ivec);
                results.add(Results.create(item, score));
            }
        }

        return Results.newResultMap(results);
    }
}

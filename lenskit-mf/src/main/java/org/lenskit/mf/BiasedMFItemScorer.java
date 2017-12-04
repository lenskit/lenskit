/*
 * LensKit, an open-source toolkit for recommender systems.
 * Copyright 2014-2017 LensKit contributors (see CONTRIBUTORS.md)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.lenskit.mf;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongIterators;
import org.apache.commons.math3.linear.RealVector;
import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.bias.BiasModel;
import org.lenskit.results.Results;
import org.lenskit.util.collections.LongUtils;
import org.lenskit.util.math.Vectors;

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
    private final BiasModel biasModel;

    /**
     * Create a new biased MF item scorer.
     * @param mod The model (factorized matrix)
     * @param bias The bias model to use.
     */
    @Inject
    public BiasedMFItemScorer(MFModel mod, BiasModel bias) {
        model = mod;
        biasModel = bias;
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

    /**
     * Compute the score a user and item using their vectors.
     *
     * @param bias The combined user-item bias term (the baseline score, usually).
     * @param user The user-factor vector.
     * @param item The item-factor vector.
     * @return The kernel function value (combined score).
     * @throws IllegalArgumentException if the user and item vectors have different lengths.
     */
    protected double computeScore(double bias, @Nonnull RealVector user, @Nonnull RealVector item) {
        return bias + user.dotProduct(item);
    }

    public MFModel getModel() {
        return model;
    }

    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items) {
        Long2DoubleMap baselines = biasModel.getItemBiases(LongUtils.packedSet(items));
        baselines = Vectors.addScalar(baselines, biasModel.getIntercept() + biasModel.getUserBias(user));

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
                double score = computeScore(baselines.get(item), uvec, ivec);
                results.add(Results.create(item, score));
            }
        }

        return Results.newResultMap(results);
    }
}
